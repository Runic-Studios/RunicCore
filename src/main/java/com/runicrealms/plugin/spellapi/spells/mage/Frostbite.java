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
public class Frostbite extends Spell {

    private static final int DURATION = 4;
    private static final int MAX_DIST = 10;

    public Frostbite() {
        super("Frostbite",
                "You conjure icy tendrils at " +
                        "your target location for " + DURATION +
                        "s, snaring enemies caught " +
                        "in the frost!",
                ChatColor.WHITE, ClassEnum.MAGE, 15, 30);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // throw the
        Location lookLoc = pl.getTargetBlock(null, MAX_DIST).getLocation();
        Block lookLocBlock = lookLoc.getBlock();

        while (lookLocBlock.getType() != Material.AIR)
            lookLocBlock = lookLocBlock.getRelative(BlockFace.UP);

        pl.getWorld().playSound(lookLocBlock.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);

        List<Block> cobWebShape = blocksToChange(lookLocBlock);
        List<Block> blocksToRevert = new ArrayList<>();
        for (Block b : cobWebShape) {
            if (b.getType() == Material.AIR) {
                b.setType(Material.COBWEB);
                pl.getWorld().spawnParticle(Particle.REDSTONE, lookLocBlock.getLocation(),
                        25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 2));
                pl.getWorld().spawnParticle(Particle.REDSTONE, lookLocBlock.getLocation(),
                        25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 2));
                blocksToRevert.add(b);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : blocksToRevert) {
                    b.setType(Material.AIR);
                }
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION * 20L); // todo: make this run before server shutdown? will it?
    }

    /**
     * Returns a list of potential blocks to change to webs
     * @param lookBlock target block player is looking at
     * @return a 3x3 square of blocks surrounding target block
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

