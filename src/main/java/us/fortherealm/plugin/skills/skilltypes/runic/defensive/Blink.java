package us.fortherealm.plugin.skills.skilltypes.runic.defensive;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.Skill;

@SuppressWarnings("FieldCanBeLocal")
public class Blink extends Skill {

    // instance variables
    private static int MAX_DIST = 10;

    // constructor
    public Blink() {
        super("Blink", "You blink", 4);
    }

    @Override
    public void executeSkill() {

        Location loc = getPlayer().getLocation();
        Block validFinalBlock = null;
        Block currentBlock;

        // make sure the player is blinking to a valid location
        BlockIterator iter = null;
        try {
            iter = new BlockIterator(getPlayer(), MAX_DIST);
        }
        catch (IllegalStateException e) {
            getPlayer().sendMessage(ChatColor.RED + "You cannot blink here!");
        }
        while (iter.hasNext()) {
            currentBlock = iter.next();
            Material currentBlockType = currentBlock.getType();

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
        Location teleportLoc = validFinalBlock.getLocation().clone();
        teleportLoc.add(new Vector(.5, 0, .5));

        // Set the blink location yaw/pitch to the player's
        teleportLoc.setPitch(loc.getPitch());
        teleportLoc.setYaw(loc.getYaw());

        // particles, sounds
        getPlayer().getWorld().spawnParticle(Particle.REDSTONE, getPlayer().getLocation().add(0,1,0),
                25, 0, 0.2f, 0.2f, 0.2f, new Particle.DustOptions(Color.FUCHSIA, 15));
        getPlayer().getWorld().spawnParticle(Particle.REDSTONE, teleportLoc.add(0,1,0),
                25, 0, 0.2f, 0.2f, 0.2f, new Particle.DustOptions(Color.FUCHSIA, 15));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);

        // teleport the player to the blink location
        getPlayer().teleport(teleportLoc);
    }
}
