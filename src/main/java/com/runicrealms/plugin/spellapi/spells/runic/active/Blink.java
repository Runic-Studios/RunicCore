package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Blink extends Spell {

    // instance variables
    private static final int MAX_DIST = 8;

    // constructor
    public Blink() {
        super("Blink",
                "You teleport forward, up to" +
                        "\na distance of " + MAX_DIST + " blocks!",
                ChatColor.WHITE, ClassEnum.RUNIC, 10, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        Location loc = pl.getLocation();
        Block validFinalBlock = null;
        Block currentBlock;

        // make sure the player is blinking to a valid location
        BlockIterator iter = null;
        try {
            iter = new BlockIterator(pl, MAX_DIST);
        }
        catch (IllegalStateException e) {
            pl.sendMessage(ChatColor.RED + "You cannot blink here!");
        }
        while (iter.hasNext()) {

            currentBlock = iter.next();
            Material currentBlockType = currentBlock.getType();

            if (currentBlockType == Material.BARRIER
                    || currentBlock.getRelative(BlockFace.UP).getType() == Material.BARRIER) {
                break;
            }

            if (currentBlockType.isTransparent()) {
                if (currentBlock.getRelative(BlockFace.UP).getType().isTransparent()) {
                    validFinalBlock = currentBlock;
                }
            }
            else {
                break;
            }
        }

        // create the blink location
        try {
            Location teleportLoc = validFinalBlock.getLocation().clone();
            teleportLoc.add(new Vector(.5, 0, .5));

            // Set the blink location yaw/pitch to the player's
            teleportLoc.setPitch(loc.getPitch());
            teleportLoc.setYaw(loc.getYaw());

            // particles, sounds
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation().add(0, 1, 0),
                    10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
            pl.getWorld().spawnParticle(Particle.REDSTONE, teleportLoc.add(0, 1, 0),
                    10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.FUCHSIA, 5));
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);

            // teleport the player to the blink location
            pl.teleport(teleportLoc);
            VectorUtil.drawLine(pl, Particle.REDSTONE, Color.FUCHSIA, loc, teleportLoc, 1.0);
            final Vector velocity = pl.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(0.5);
            pl.setVelocity(velocity);

            new BukkitRunnable() {
                @Override
                public void run() {

                    if (pl.isOnGround()) {
                        this.cancel();
                    } else {
                        pl.setFallDistance(-8.0F);
                    }
                }
            }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 1L);
        } catch (NullPointerException e) {
            pl.sendMessage(ChatColor.RED + "Error: blink location invalid!");
        }
    }
}

