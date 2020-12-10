package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.group.GroupPurpose;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
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

public class GroupCreateChooseTypeGui implements Listener {

    private static final Set<Player> viewers = new HashSet<>();
    private static Inventory inventory;
    private static final ItemStack backArrow = GUIUtil.dispItem(Material.ARROW, "&cBack");

    public static void initInventory() {
        inventory = Bukkit.createInventory(null, 27, "Choose Group Type");
        inventory.setItem(0, backArrow);
        inventory.setItem(4, GUIUtil.dispItem(Material.PAPER, "&eChoose Group Type", new String[] {
                "&7When creating a group, you must choose",
                "&7the purpose of the group. There are",
                "&7sub-purposes under each of these categories."
        }));
        inventory.setItem(10, GroupPurpose.Type.DUNGEON.getIcon());
        inventory.setItem(12, GroupPurpose.Type.QUESTS.getIcon());
        inventory.setItem(14, GroupPurpose.Type.GRINDING.getIcon());
        inventory.setItem(16, GroupPurpose.Type.MINIBOSS.getIcon());
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
                if (event.getView().getTitle().equals("Choose Group Type")) {
                    event.setCancelled(true);
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        if (event.getSlot() == 0) {
                            viewers.remove(player);
                            GroupMainGui.display(player);
                        } else if (event.getSlot() == 10) {
                            viewers.remove(player);
                            GroupCreateChoosePurposeGui.display(player, GroupPurpose.Type.DUNGEON);
                        } else if (event.getSlot() == 12) {
                            viewers.remove(player);
                            GroupCreateChoosePurposeGui.display(player, GroupPurpose.Type.QUESTS);
                        } else if (event.getSlot() == 14) {
                            viewers.remove(player);
                            GroupCreateChoosePurposeGui.display(player, GroupPurpose.Type.GRINDING);
                        } else if (event.getSlot() == 16) {
                            viewers.remove(player);
                            GroupCreateChoosePurposeGui.display(player, GroupPurpose.Type.MINIBOSS);
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
