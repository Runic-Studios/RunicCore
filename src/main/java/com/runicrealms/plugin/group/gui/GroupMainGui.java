package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
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

public class GroupMainGui implements Listener {

    private static final Set<Player> viewers = new HashSet<>();
    private static Inventory inventory;
    private static final String invTitle = ChatColor.YELLOW + "" + ChatColor.BOLD + "Groups";

    public static void initInventory() {
        inventory = Bukkit.createInventory(null, 27, invTitle);
        inventory.setItem(11, GUIItem.dispItem(Material.WRITABLE_BOOK, "&eCreate a Group", new String[] {
                "&7Groups are a way for you complete",
                "&7challenging content with other players,",
                "&7like running a dungeon or fighting a boss."
        }));
        inventory.setItem(13, GUIItem.dispItem(Material.IRON_SWORD, "&eJoin a Group", new String[] {
                "&7Join another player's group to complete",
                "&7a common goal, like finishing a quest",
                "&7or fighting a boss!"
        }));
        inventory.setItem(15, GUIItem.dispItem(Material.PAPER, "&eInfo on Your Group", new String[] {
                "&7Get info on your current group!"
        }));
    }

    public static void display(Player player) {
        player.closeInventory();
        player.openInventory(inventory);
        viewers.add(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.contains(player)) {
                if (event.getView().getTitle().equals(invTitle)) {
                    event.setCancelled(true);
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        if (event.getSlot() == 11) {
                            viewers.remove(player);
                            if (RunicCore.getGroupManager().canJoinGroup(player)) {
                                GroupCreateChooseTypeGui.display(player);
                            } else {
                                player.closeInventory();
                                player.sendMessage(ChatColor.RED + "You are already in a group/party!");
                            }
                        } else if (event.getSlot() == 13) {
                            viewers.remove(player);
                            if (RunicCore.getGroupManager().canJoinGroup(player)) {
                                GroupJoinGui.display(player, 0);
                            } else {
                                player.closeInventory();
                                player.sendMessage(ChatColor.RED + "You are already in a group/party!");
                            }
                        } else if (event.getSlot() == 15) {
                            viewers.remove(player);
                            GroupInfoGui.display(player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        viewers.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer());
    }

}
