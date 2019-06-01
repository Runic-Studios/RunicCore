package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class ShadowStep extends Spell {

    private static final int DURATION = 10;

    public ShadowStep() {
        super("Shadow Step",
                "You mark your current location" +
                        "\nin shadow! After " + DURATION + " seconds, you" +
                        "\nteleport back to the marked" +
                        "\nlocation!", ChatColor.WHITE,20, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.PURPLE, 3));
                    pl.teleport(loc);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                            new Particle.DustOptions(Color.PURPLE, 3));
                } else {
                    count += 1;
                    createCircle(pl, loc, 1f);
                }

            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    private void createCircle(Player pl, Location loc, float radius) {

        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc,
                    1, 0, 0, 0, 0, new Particle.DustOptions(Color.PURPLE, 1));
            pl.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }
    }
}

