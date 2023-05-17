package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class Whirlwind extends Spell implements DurationSpell, PhysicalDamageSpell, RadiusSpell {
    private static final HashSet<UUID> playerUuidSet = new HashSet<>();
    private double damageAmt;
    private double damagePerLevel;
    private double duration;
    private double radius;

    public Whirlwind() {
        super("Whirlwind", CharacterClass.WARRIOR);
        this.setDescription("For " + duration + " seconds, you unleash the " +
                "fury of the winds, summoning a cyclone around you that damages " +
                "enemies within " + radius + " blocks for (" +
                damageAmt + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ damage!");
    }

    public static HashSet<UUID> getUuidSet() {
        return playerUuidSet;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        playerUuidSet.add(player.getUniqueId());
        final Spell spell = this;

        BukkitRunnable whirlwind = new BukkitRunnable() {
            @Override
            public void run() {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.5f);
                new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.CLOUD, player.getEyeLocation());

                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
                    DamageUtil.damageEntityPhysical(damageAmt, (LivingEntity) entity, player, false, false, spell);
                }
            }
        };
        whirlwind.runTaskTimer(RunicCore.getInstance(), 0, 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                    whirlwind.cancel();
                    playerUuidSet.remove(player.getUniqueId());
                },
                (int) duration * 20L);
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
    public double getPhysicalDamage() {
        return damageAmt;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damageAmt = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

}
