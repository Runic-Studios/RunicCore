package me.skyfallin.plugin.skill.skills;

import me.skyfallin.plugin.skill.skilltypes.Skill;
import me.skyfallin.plugin.skill.skilltypes.SkillItemType;
import me.skyfallin.plugin.util.BlocksUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Blink extends Skill {

    public Blink() {
        super("Blink", "You blink", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 8);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {

        Location loc = player.getLocation();
        int distance = 10;
        Block validFinalBlock = null;
        Block currentBlock;

        BlockIterator iter = null;
        try {
            iter = new BlockIterator(player, distance);
        }
        catch (IllegalStateException e) {
            player.sendMessage(ChatColor.RED + "You cannot blink here!");
        }
        while (iter.hasNext()) {
            currentBlock = iter.next();
            Material currentBlockType = currentBlock.getType();

            if (BlocksUtil.transparentBlocks.contains(currentBlockType)) {
                if (BlocksUtil.transparentBlocks.contains(currentBlock.getRelative(BlockFace.UP).getType())) {
                    validFinalBlock = currentBlock;
                }
            }
            else {
                break;
            }
        }
        Location teleportLoc = validFinalBlock.getLocation().clone();
        teleportLoc.add(new Vector(.5, 0, .5));

        // Set the blink location yaw/pitch to that of the player
        teleportLoc.setPitch(loc.getPitch());
        teleportLoc.setYaw(loc.getYaw());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 0.5f, 1.2f);
        player.getWorld().spigot().playEffect(player.getLocation(),
                Effect.WITCH_MAGIC, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);
        player.getWorld().spigot().playEffect(teleportLoc,
                Effect.WITCH_MAGIC, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);
        player.teleport(teleportLoc);
    }
}
