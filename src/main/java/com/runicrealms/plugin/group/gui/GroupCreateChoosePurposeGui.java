package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.GroupPurpose;
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
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupCreateChoosePurposeGui implements Listener {

    private static Map<Player, Map<Integer, GroupPurpose>> viewers = new HashMap<Player, Map<Integer, GroupPurpose>>();
    private static ItemStack backArrow = GUIItem.dispItem(Material.ARROW, "&cBack");

    public static void display(Player player, GroupPurpose.Type type) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Group Purpose - " + type.getName());
        inventory.setItem(0, backArrow);
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
        player.closeInventory();
        player.openInventory(inventory);
        viewers.put(player, slots);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.containsKey(player)) {
                if (event.getView().getTitle().matches("Group Purpose - .*")) {
                    event.setCancelled(true);
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        if (event.getSlot() == 0) {
                            viewers.remove(player);
                            GroupCreateChooseTypeGui.display(player);
                        } else if (viewers.get(player).containsKey(event.getSlot())) {
                            if (RunicCore.getGroupManager().canJoinGroup(player)) {
                                if (!RunicCore.getGroupManager().getGroups().containsKey(viewers.get(player).get(event.getSlot()))) {
                                    RunicCore.getGroupManager().createGroup(player, viewers.get(player).get(event.getSlot()));
                                    RunicCore.getGroupManager().getPlayerGroup(player).sendMessageInChannel(player.getName() + " has activated this group!");
                                    player.closeInventory();
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