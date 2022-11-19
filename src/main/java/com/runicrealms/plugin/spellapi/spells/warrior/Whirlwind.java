package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Whirlwind extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 4;
    private static final double DAMAGE_PER_LEVEL = .25;
    private static final int DURATION = 10;
    private static final float RADIUS = 2f;

    // constructor
    public Whirlwind() {
        super("Whirlwind",
                "For " + DURATION + " seconds, you unleash the " +
                        "fury of the winds, summoning a cyclone around you that damages " +
                        "enemies within " + (double) RADIUS + " blocks for (" +
                        DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 20, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        final Spell spell = this;

        BukkitRunnable whirlwind = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> spawnCycloneParticle(player));

                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS)) {
                    if (!(entity instanceof LivingEntity)) continue;
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (!isValidEnemy(player, entity)) continue;
                    DamageUtil.damageEntitySpell(DAMAGE_AMT, livingEntity, player, spell);
                }
            }
        };
        whirlwind.runTaskTimer(RunicCore.getInstance(), 0, 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), whirlwind::cancel, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /**
     * @param player
     * @param location
     * @param x
     * @param z
     */
    private void spawnCloud(Player player, Location location, double x, double z) {
        location.add(x, 0, z);
        player.getWorld().spawnParticle(Particle.CLOUD, location, 1, 0, 0, 0, 0);
        location.subtract(x, 0, z);
    }

    /**
     * @param player
     */
    private void spawnCycloneParticle(Player player) {

        Location location = player.getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.75f, 0.5f);

        Location location1 = player.getEyeLocation();
        Location location2 = player.getEyeLocation().add(0, 1, 0);
        Location location3 = player.getEyeLocation().subtract(0, 1, 0);
        int particles = 50;
        float radius = RADIUS;

        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            spawnCloud(player, location1, x, z);
            spawnCloud(player, location2, x, z);
            spawnCloud(player, location3, x, z);
        }
    }
}
