package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.Bukkit;
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

public class GroupMainGui implements Listener {

    private static Set<Player> viewers = new HashSet<Player>();
    private static Inventory inventory;

    public static void initInventory() {
        inventory = Bukkit.createInventory(null, 27, "Groups");
        inventory.setItem(11, GUIItem.dispItem(Material.WRITABLE_BOOK, "&eCreate a Group", new String[] {
                "&7Groups are a way for you to play with",
                "&7other players  while having a set purpose,",
                "&7like running a dungeon or fighting a boss."
        }));
        inventory.setItem(11, GUIItem.dispItem(Material.WRITABLE_BOOK, "&eJoin a Group", new String[] {
                "&7Join another player's group to play",
                "&7with them and others while having a set purpose,",
                "&7like running a dungeon or fighting a boss."
        }));
        inventory.setItem(11, GUIItem.dispItem(Material.WRITABLE_BOOK, "&eInfo on Your Group", new String[] {
                "&7Get info on the group you are currently in"
        }));
    }

    public static void display(Player player) {
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
                    if (event.getSlot() == 11) {
                        GroupCreateChooseTypeGui.display(player);
                    } else if (event.getSlot() == 13) {
                        GroupJoinGui.display(player, 0);
                    } else if (event.getSlot() == 15) {
                        GroupInfoGui.display(player);
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
