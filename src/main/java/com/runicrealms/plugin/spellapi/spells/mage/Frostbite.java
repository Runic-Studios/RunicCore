package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
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
                ChatColor.WHITE, CharacterClass.MAGE, 15, 30);
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
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                            () -> {
                                new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.CLOUD, finalTargetBlockLocation);
                                new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.REDSTONE, finalTargetBlockLocation, Color.AQUA);
                            });
                    player.getWorld().playSound(finalTargetBlockLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                    for (Entity en : player.getWorld().getNearbyEntities(finalTargetBlockLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!(isValidEnemy(player, en))) continue;
                        if (isRooted(en)) continue;
                        LivingEntity victim = (LivingEntity) en;
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                        addStatusEffect(victim, RunicStatusEffect.ROOT, (DURATION + 2) - count, true); // root for remaining duration
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }
}

