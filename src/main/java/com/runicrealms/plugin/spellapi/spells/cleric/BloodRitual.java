package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.HashSet;
import java.util.Set;

public class BloodRitual extends Spell {

    private static final int HEALING_AMT = 60;
    private static final int MAX_DIST = 10;
    private static final float RADIUS = 5f;
    private static final int WARMUP = 4;

    // constructor
    public BloodRitual() {
        super("Blood Ritual",
                "You conjure an unholy lectern" +
                        "\nthat charges for " + WARMUP + " seconds!" +
                        "\nAfter, it explodes, restoring✦" +
                        "\n" + HEALING_AMT + " health to allies within" +
                        "\n" + (int) RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.swingMainHand();

        // spawn lectern at loc
//        Location lecternLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
//        // ensure lectern location is on ground
//        while (lecternLoc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
//            lecternLoc = lecternLoc.getBlock().getRelative(BlockFace.DOWN).getLocation();
//        // move it one block up
//        lecternLoc.setY(pl.getLocation().clone().add(0, 1, 0).getY());
        Location lecternLoc = getFurthestAirBlock(pl, MAX_DIST).getLocation();
        while (lecternLoc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            lecternLoc = lecternLoc.getBlock().getRelative(BlockFace.DOWN).getLocation();
        }
        BlockData data = lecternLoc.getBlock().getBlockData();
        final Set<Player> players = new HashSet<Player>();
        for (Player loaded : RunicCore.getCacheManager().getLoadedPlayers()) {
            if (loaded.getLocation().getWorld() == lecternLoc.getWorld()) {
                if (loaded.getLocation().distanceSquared(lecternLoc) <= Math.pow(50, 2)) {
                    loaded.sendBlockChange(lecternLoc, Material.LECTERN.createBlockData());
                    players.add(loaded);
                }
            }
        }

        // warmup block
        Location finalLecternLoc = lecternLoc;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > WARMUP) {
                    this.cancel();
                    pl.getWorld().playSound(finalLecternLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, finalLecternLoc, 1, 0, 0, 0, 0);
                    for (Player player : players) {
                        player.sendBlockChange(finalLecternLoc, data);
                    }
                    for (Entity ally : pl.getWorld().getNearbyEntities(finalLecternLoc, RADIUS, RADIUS, RADIUS)) {
                        if (!(ally instanceof Player))
                            continue;
                        if (verifyAlly(pl, ally))
                            HealUtil.healPlayer(HEALING_AMT, (Player) ally, pl, true, false, false);
                    }
                } else {
                    count += 1;
                    pl.getWorld().playSound(finalLecternLoc, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, finalLecternLoc,
                            25, 1f, 1f, 1f, new Particle.DustOptions(Color.RED, 1));
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }

    private static Block getFurthestAirBlock(Player player, int range) {
        BlockIterator iterator = new BlockIterator(player, range);
        Block lastBlock = iterator.next();
        while (iterator.hasNext()) {
            Block currentBlock = iterator.next();
            if (currentBlock.getType() == Material.AIR) {
                lastBlock = currentBlock;
            } else {
                break;
            }
        }
        return lastBlock;
    }

}
