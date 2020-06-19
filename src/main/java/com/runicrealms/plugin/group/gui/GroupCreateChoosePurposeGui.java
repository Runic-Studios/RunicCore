package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.GroupPurpose;
import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GroupCreateChoosePurposeGui implements Listener {

    private static Map<Player, Map<Integer, GroupPurpose>> viewers = new HashMap<Player, Map<Integer, GroupPurpose>>();

    public static void display(Player player, GroupPurpose.Type type) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Group Purpose - " + type.getName());
        inventory.setItem(4, type.getIcon());
        Map<Integer, GroupPurpose> slots = new HashMap<Integer, GroupPurpose>();
        int slot = 9;
        for (GroupPurpose purpose : GroupPurpose.values()) {
            if (purpose.getType() == type) {
                ItemStack icon = purpose.getIcon();
                if (RunicCore.getGroupManager().getGroups().containsKey(purpose)) {
                    GUIItem.setName(icon, icon.getItemMeta().getDisplayName() + " &c&lACTIVE");
                }
                inventory.setItem(slot, icon);
                slots.put(slot, purpose);
                slot++;
            }
        }
        player.openInventory(inventory);
        viewers.put(player, slots);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.containsKey(player)) {
                event.setCancelled(true);
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    if (viewers.get(player).containsKey(event.getSlot())) {
                        if (RunicCore.getGroupManager().canJoinGroup(player)) {
                            if (!RunicCore.getGroupManager().getGroups().containsKey(viewers.get(player).get(event.getSlot()))) {
                                // TODO - create group
                            } else {
                                player.closeInventory();
                                player.sendMessage(ChatColor.RED + "A group with that purpose has already been created!");
                            }
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "You cannot create a group because you are in a group/party!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (viewers.containsKey(event.getPlayer())) {
            viewers.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (viewers.containsKey(event.getPlayer())) {
            viewers.remove(event.getPlayer());
        }
    }

}