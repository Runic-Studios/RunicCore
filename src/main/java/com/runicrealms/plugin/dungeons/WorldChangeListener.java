package com.runicrealms.plugin.dungeons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This method clears 'dungeon keys' from player inventories when they leave the dungeon world
 * @author Skyfallin_
 */
public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {

        if (e.getFrom().getName().toLowerCase().equals("dungeons")) {

            Player pl = e.getPlayer();

            ItemStack[] inv = pl.getInventory().getContents();

            boolean hasSentMessage = false;
            for (ItemStack itemStack : inv) {

                if (itemStack == null) continue;
                if (!itemStack.hasItemMeta()) continue;
                if (itemStack.getType() == Material.PRISMARINE_CRYSTALS || itemStack.getType() == Material.PRISMARINE_SHARD) {
                    itemStack.setAmount(0);
                    if (!hasSentMessage) {
                        pl.sendMessage(ChatColor.GRAY + "Your dungeon items have been removed.");
                        hasSentMessage = true;
                    }
                }
            }
        }
    }
}
