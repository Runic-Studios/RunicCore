package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Judgment extends Spell implements DurationSpell, HealingSpell, RadiusSpell {
    private static final double UPDATES_PER_SECOND = 4;
    private double bubbleDuration;
    private double heal;
    private double healingPerLevel;
    private double knockbackMultiplier;
    private double radius;

    public Judgment() {
        super("Judgment", CharacterClass.WARRIOR);
        this.setDescription("You summon a barrier of magic " +
                "around yourself for " + bubbleDuration + "s, instantly knocking away all enemies! The barrier " +
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

        // Heal caster, look for targets nearby
        BukkitTask healTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            healPlayer(player, player, heal, this);
            for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                if (isValidEnemy(player, entity)) {
                    Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-knockbackMultiplier).setY(0.3);
                    entity.setVelocity(force);
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
                } else if (isValidAlly(player, entity)) {
                    healPlayer(player, (Player) entity, heal, this);
                }
            }
        }, 0, 20L);

        // Begin spell event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // Create visual bubble
                phi += Math.PI / 10;
                Location loc = player.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = radius * cos(theta) * sin(phi);
                    double y = radius * cos(phi) + 1.5;
                    double z = radius * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    player.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }
                
                // Radius indicator
                new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.VILLAGER_HAPPY, loc, Color.GREEN);

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > bubbleDuration * 1000 || player.isSneaking()) {
                    healTask.cancel();
                    this.cancel();
                    removeStatusEffect(player, RunicStatusEffect.ROOT);
                    return;
                }

                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 2.0f);

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
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number knockback = (Number) spellData.getOrDefault("knockback", 0);
        setKnockbackMultiplier(knockback.doubleValue());
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setKnockbackMultiplier(double knockbackMultiplier) {
        this.knockbackMultiplier = knockbackMultiplier;
    }

}

