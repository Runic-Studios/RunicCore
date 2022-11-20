package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Frostbite extends Spell {

    private static final int DURATION = 4;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 4;

    public Frostbite() {
        super("Frostbite",
                "You conjure icy tendrils at " +
                        "your target location for " + DURATION +
                        "s, rooting enemies caught " +
                        "in the frost!",
                ChatColor.WHITE, ClassEnum.MAGE, 15, 30);
    }

    private void createCircle(Player player, Location loc) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) RADIUS;
            z = Math.sin(angle) * (float) RADIUS;
            loc.add(x, 0, z);
            player.getWorld().spawnParticle(Particle.CLOUD, loc, 5, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location lookLocation = player.getTargetBlock(null, MAX_DIST).getLocation();
        Block targetBlockLocation = lookLocation.getBlock();

        while (targetBlockLocation.getType() != Material.AIR)
            targetBlockLocation = targetBlockLocation.getRelative(BlockFace.UP);

        Location finalTargetBlockLocation = targetBlockLocation.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += 1;
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> createCircle(player, finalTargetBlockLocation));
                    player.getWorld().playSound(finalTargetBlockLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                    for (Entity en : player.getWorld().getNearbyEntities(finalTargetBlockLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!(isValidEnemy(player, en))) continue;
                        if (isRooted(en)) continue;
                        LivingEntity victim = (LivingEntity) en;
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                        addStatusEffect(victim, EffectEnum.ROOT, (DURATION + 2) - count); // root for remaining duration
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }
}

