package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.Group;
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
import java.util.Iterator;
import java.util.Map;

public class GroupJoinGui implements Listener {

    private static Map<Player, PlayerGuiInfo> viewers = new HashMap<Player, PlayerGuiInfo>();
    private static ItemStack blankSlot = GUIItem.dispItem(Material.BLACK_STAINED_GLASS_PANE, " ", new String[] {});
    private static ItemStack previousArrow = GUIItem.dispItem(Material.ARROW, "&ePrevious Page", new String[] {});
    private static ItemStack nextArrow = GUIItem.dispItem(Material.ARROW, "&eNext Page", new String[] {});
    private static ItemStack guiIcon = GUIItem.dispItem(Material.IRON_SWORD, "&eJoin a Group", new String[] {});

    public static void display(Player player, Integer page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Join a Group");
        inventory.setItem(0, page > 0 ? previousArrow : blankSlot);
        inventory.setItem(8, RunicCore.getGroupManager().getGroups().size() > page * 45 + 45 ? nextArrow : blankSlot);
        for (int i = 1; i < 8; i++) {
            inventory.setItem(i, i != 4 ? blankSlot : guiIcon);
        }
        if (RunicCore.getGroupManager().getGroups().size() > 0) {
            Iterator<Map.Entry<GroupPurpose, Group>> iterator = RunicCore.getGroupManager().getGroups().entrySet().iterator();
            int slot = 9;
            int count = 0;
            Map<Integer, Group> slots = new HashMap<Integer, Group>();
            while (iterator.hasNext()) {
                if (count >= page * 45 && count < RunicCore.getGroupManager().getGroups().size() - page * 45) {
                    Group group = iterator.next().getValue();
                    inventory.setItem(slot, group.getIcon());
                    slots.put(slot, group);
                    slot++;
                } else {
                    iterator.next();
                }
                count++;
            }
            player.openInventory(inventory);
            viewers.put(player, new PlayerGuiInfo(page, slots));
        } else {
            inventory.setItem(13, GUIItem.dispItem(Material.BARRIER, "&cNo Active Groups", new String[] {}));
            player.openInventory(inventory);
            viewers.put(player, new PlayerGuiInfo(page, new HashMap<Integer, Group>()));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.containsKey(player)) {
                event.setCancelled(true);
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    if (event.getSlot() == 0 && event.getCurrentItem().getType() == Material.ARROW) {
                        display(player, viewers.get(player).getPage() - 1);
                    } else if (event.getSlot() == 8 && event.getCurrentItem().getType() == Material.ARROW) {
                        display(player, viewers.get(player).getPage() + 1);
                    } else if (viewers.get(player).getSlots().containsKey(event.getSlot())) {
                        if (RunicCore.getGroupManager().getGroups().containsKey(viewers.get(player).getSlots().get(event.getSlot()).getPurpose())) {
                            RunicCore.getGroupManager().addToGroup(player, viewers.get(player).getSlots().get(event.getSlot()));
                            player.closeInventory();
                            // TODO - send message in channel
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "That group is no longer active!");
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

    private static class PlayerGuiInfo {

        private Integer page;
        private Map<Integer, Group> slots;

        public PlayerGuiInfo(Integer page, Map<Integer, Group> slots) {
            this.page = page;
            this.slots = slots;
        }

        public Integer getPage() {
            return this.page;
        }

        public Map<Integer, Group> getSlots() {
            return this.slots;
        }

    }

}
