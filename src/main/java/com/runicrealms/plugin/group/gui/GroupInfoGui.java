package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.Group;
import com.runicrealms.plugin.utilities.GUIUtil;
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

import java.util.HashSet;
import java.util.Set;

public class GroupInfoGui implements Listener {

    private static final Set<Player> viewers = new HashSet<>();
    private static final ItemStack backArrow = GUIUtil.dispItem(Material.ARROW, "&cBack");

    public static void display(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Group Info");
        inventory.setItem(0, backArrow);
        if (RunicCore.getGroupManager().getPlayerGroup(player) != null) {
            Group group = RunicCore.getGroupManager().getPlayerGroup(player);
            inventory.setItem(3, group.getIcon());
            inventory.setItem(5, GUIUtil.dispItem(Material.BARRIER, "&cLeave your group", new String[] {}));
            int slot = 9;
            for (Player member : group.getMemberIcons().keySet()) {
                inventory.setItem(slot, group.getMemberIcons().get(player));
                slot++;
            }
        } else {
            inventory.setItem(13, GUIUtil.dispItem(Material.BARRIER, "&cYou are not in a group", new String[] {
                    "&7Group members and more info would show up here"
            }));
        }
        player.closeInventory();
        player.openInventory(inventory);
        viewers.add(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.contains(player)) {
                if (event.getView().getTitle().equals("Group Info")) {
                    event.setCancelled(true);
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        if (event.getSlot() == 0) {
                            viewers.remove(player);
                            GroupMainGui.display(player);
                        } else if (event.getSlot() == 5) {
                            if (RunicCore.getGroupManager().getPlayerGroup(player) != null) {
                                RunicCore.getGroupManager().getPlayerGroup(player).sendMessageInChannel(player.getName() + " has left the group!");
                                RunicCore.getGroupManager().removeFromGroup(player, RunicCore.getGroupManager().getPlayerGroup(player));
                                player.closeInventory();
                            } else {
                                player.closeInventory();
                                player.sendMessage(ChatColor.RED + "You are not in a group!");
                            }
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