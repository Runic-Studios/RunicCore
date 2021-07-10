package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Rift extends Spell {

    private static final int DURATION = 4;
    private static final int RADIUS = 5;

    public Rift() {
        super("Rift",
                "You summon a portal of punishing magic, " +
                        "drawing in all enemies within " + RADIUS + " blocks " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 25, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        Location castLocation = pl.getLocation();
        while (castLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            castLocation = castLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
        Location finalCastLocation = castLocation;
        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count++;
                    spawnRift(pl, finalCastLocation);
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void spawnRift(Player pl, Location castLocation) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_CAT_HISS, 0.5F, 0.5F);

        // create circle
        createCircle(pl, castLocation, RADIUS);

        // create smaller circles
        createCircle(pl, castLocation, (int) (RADIUS * 0.6));
        createCircle(pl, castLocation, (int) (RADIUS * 0.2));

        for (Entity en : pl.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(pl, en)) continue;
            LivingEntity victim = (LivingEntity) en;
            victim.teleport(castLocation);
        }
    }

    private void createCircle(Player pl, Location loc, float radius) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.PURPLE, 1));
            loc.subtract(x, 0, z);
        }
    }
}

