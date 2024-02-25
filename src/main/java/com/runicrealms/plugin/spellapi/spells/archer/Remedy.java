package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Remedy extends Spell implements DurationSpell, HealingSpell, RadiusSpell {
    private static final int PARTICLES = 50; //Particles to display
    private double radius;
    private double healAmt;
    private double healingPerLevel;
    private double duration;

    public Remedy() {
        super("Remedy", CharacterClass.ARCHER);
        this.setDescription("You and allies within " + this.radius +
                " blocks are healed✦ for (" +
                this.healAmt + " + &f" + this.healingPerLevel + "x&7 lvl) health over " + this.duration + "s!");
    }

    private void createSphere(Player player, Location loc) {
        for (int i = 0; i < PARTICLES; i++) {
            Vector vector = getRandomVector().multiply(radius);
            loc.add(vector);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0);
            loc.subtract(vector);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Spell spell = this;

        AtomicInteger count = new AtomicInteger(1);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > duration) {
                task.cancel();
            } else {
                count.set(count.get() + 1);

                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_STEP, 0.5f, 0.2f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PARROT_AMBIENT, 0.5f, 0.2f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 0.2f);
                createSphere(player, player.getEyeLocation());
                healPlayer(player, player, healAmt, spell);
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity.equals(player)) continue;
                    if (!TargetUtil.isValidAlly(player, entity)) continue;
                    Player playerEntity = (Player) entity;
                    healPlayer(player, playerEntity, healAmt / duration, spell);
                }
            }
        }, 0, 20L);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getHeal() {
        return (int) healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
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

    private Vector getRandomVector() {
        double x, y, z;
        x = ThreadLocalRandom.current().nextDouble() * 2 - 1;
        y = ThreadLocalRandom.current().nextDouble() * 2 - 1;
        z = ThreadLocalRandom.current().nextDouble() * 2 - 1;
        return new Vector(x, y, z).normalize();
    }
}
