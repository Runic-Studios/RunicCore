package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class Whirlwind extends Spell {

    private static final int DAMAGE_AMT = 35;
    private static final int DURATION = 6;
    private static final float RADIUS = 2.5f;

    // constructor
    public Whirlwind() {
        super("Whirlwind",
                "For " + DURATION + " seconds, you unleash the " +
                        "fury of the winds, summoning a cyclone around you that damages " +
                        "enemies within " + (double) RADIUS + " blocks for " + DAMAGE_AMT + " " +
                        "spellʔ damage!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 20, 25);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // begin effect
        BukkitRunnable whirlwind = new BukkitRunnable() {
            @Override
            public void run() {
                spawnCyclone(pl);
            }
        };
        whirlwind.runTaskTimer(RunicCore.getInstance(), 0, 20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                whirlwind.cancel();
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20);
    }

    private void spawnCyclone(Player pl) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.75f, 0.5f);

        Location location1 = pl.getEyeLocation();
        int particles = 50;
        float radius = RADIUS;

        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            location1.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.CLOUD, location1, 1, 0, 0, 0, 0);
            location1.subtract(x, 0, z);
        }

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (!(en instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) en;
            if (!verifyEnemy(pl, en)) continue;
            DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, 100);
        }
    }
}
