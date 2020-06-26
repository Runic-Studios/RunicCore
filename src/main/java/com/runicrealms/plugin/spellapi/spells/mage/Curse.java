package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class Curse extends Spell {

    private static final int DURATION = 10;
    private static final int MAX_DIST = 10;
    private static final double PERCENT = 25;
    private static final int RADIUS = 10;

    public Curse() {
        super("Curse",
                "You do something!",
                ChatColor.WHITE, ClassEnum.MAGE, 1, 30); // todo: cooldown
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // throw the
        Location lookLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        Block lookLocBlock = lookLoc.getBlock();
        while (lookLocBlock.getType() != Material.AIR)
            lookLocBlock = lookLocBlock.getRelative(BlockFace.UP);
        List<Block> cobWebShape = blocksToChange(lookLocBlock);
        for (Block b : cobWebShape) {
            if (b.getType() == Material.AIR) {
                b.setType(Material.COBWEB);
                pl.getWorld().spawnParticle(Particle.REDSTONE, lookLocBlock.getLocation(),
                        25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.RED, 1));
            } else {
                cobWebShape.remove(b);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : cobWebShape) {
                    b.setType(Material.AIR);
                }
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION * 20L); // todo: make this run before server shutdown? will it?
    }

    /**
     *
     * @param lookBlock
     * @return
     */
    private List<Block> blocksToChange(Block lookBlock) {
        List<Block> blocksToChange = new ArrayList<>();
        blocksToChange.add(lookBlock);
        blocksToChange.add(lookBlock.getRelative(BlockFace.EAST));
        blocksToChange.add(lookBlock.getRelative(BlockFace.WEST));
        blocksToChange.add(lookBlock.getRelative(BlockFace.NORTH));
        blocksToChange.add(lookBlock.getRelative(BlockFace.SOUTH));
        blocksToChange.add(lookBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.WEST));
        blocksToChange.add(lookBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.WEST));
        blocksToChange.add(lookBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST));
        blocksToChange.add(lookBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST));
        return blocksToChange;
    }
}

