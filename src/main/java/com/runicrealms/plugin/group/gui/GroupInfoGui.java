package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.Group;
import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class GroupInfoGui implements Listener {

    private static Set<Player> viewers = new HashSet<Player>();

    public static void display(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Group Info");
        if (RunicCore.getGroupManager().getPlayerGroup(player) != null) {
            Group group = RunicCore.getGroupManager().getPlayerGroup(player);
            inventory.setItem(3, group.getIcon());
            inventory.setItem(5, GUIItem.dispItem(Material.BARRIER, "&cLeave your group", new String[] {}));
            int slot = 9;
            for (Player member : group.getMemberIcons().keySet()) {
                inventory.setItem(slot, group.getMemberIcons().get(player));
                slot++;
            }
        } else {
            inventory.setItem(13, GUIItem.dispItem(Material.BARRIER, "&cYou are not in a group", new String[] {
                    "&7Group members and more info would show up here"
            }));
        }
        player.openInventory(inventory);
        viewers.add(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.contains(player)) {
                event.setCancelled(true);
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    if (event.getSlot() == 5) {
                        if (RunicCore.getGroupManager().getPlayerGroup(player) != null) {
                            RunicCore.getGroupManager().removeFromGroup(player, RunicCore.getGroupManager().getPlayerGroup(player));
                            // TODO - send message in channel
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "You are not in a group!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewers.contains(event.getPlayer())) {
            viewers.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (viewers.contains(event.getPlayer())) {
            viewers.remove(event.getPlayer());
        }
    }

}