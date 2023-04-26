package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell implements DurationSpell, HealingSpell {
    private static final int BUBBLE_SIZE = 5;
    private static final double KNOCKBACK = 0.15;
    private static final double UPDATES_PER_SECOND = 5;
    private double bubbleDuration;
    private double heal;
    private double healingPerLevel;

    public Judgment() {
        super("Judgment", CharacterClass.WARRIOR);
        this.setDescription("You instantly summon a barrier of magic " +
                "around yourself for " + bubbleDuration + "s! The barrier " +
                "prevents enemies from entering, but allies may pass through freely! " +
                "Each second, allies within the barrier are healed for " +
                "(" + heal + " + &f" + healingPerLevel + "x&7 lvl) health! " +
                "During this time, you are rooted. Sneak to cancel the spell early.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean attemptToExecute(Player player) {
        if (!player.isOnGround()) {
            player.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        addStatusEffect(player, RunicStatusEffect.ROOT, bubbleDuration, true);

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        Spell spell = this;
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI / 10;
                Location loc = player.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = BUBBLE_SIZE * cos(theta) * sin(phi);
                    double y = BUBBLE_SIZE * cos(phi) + 1.5;
                    double z = BUBBLE_SIZE * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    player.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > bubbleDuration * 1000 || player.isSneaking()) {
                    this.cancel();
                    removeStatusEffect(player, RunicStatusEffect.ROOT);
                    return;
                }

                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 2.0f);

                // Look for targets nearby
                for (Entity entity : player.getNearbyEntities(BUBBLE_SIZE, BUBBLE_SIZE, BUBBLE_SIZE)) {
                    if (isValidEnemy(player, entity)) {
                        Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-KNOCKBACK).setY(0.3);
                        entity.setVelocity(force);
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                    } else if (isValidAlly(player, entity)) {
                        healPlayer(player, (Player) entity, heal, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
    }

    @Override
    public double getDuration() {
        return bubbleDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.bubbleDuration = duration;
    }

    @Override
    public double getHeal() {
        return heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

}

