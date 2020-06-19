package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.GroupPurpose;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class GroupCreateChoosePurposeGui implements Listener {

    private static Map<Player, GroupPurpose.Type> viewers = new HashMap<Player, GroupPurpose.Type>();
    private static Map<GroupPurpose.Type, PurposeInventory> inventories;

    public static void initInventories() {
        inventories = new HashMap<GroupPurpose.Type, PurposeInventory>();
        Inventory dungeon = Bukkit.createInventory(null, 54, "Group Purpose - Dungeon");
        Inventory quests = Bukkit.createInventory(null, 54, "Group Purpose - Quests");
        Inventory grinding = Bukkit.createInventory(null, 54, "Group Purpose - Grinding");
        Inventory miniboss = Bukkit.createInventory(null, 54, "Group Purpose - Miniboss");
        Map<Integer, GroupPurpose> dungeonSlots = new HashMap<Integer, GroupPurpose>();
        Map<Integer, GroupPurpose> questsSlots = new HashMap<Integer, GroupPurpose>();
        Map<Integer, GroupPurpose> grindingSlots = new HashMap<Integer, GroupPurpose>();
        Map<Integer, GroupPurpose> minibossSlots = new HashMap<Integer, GroupPurpose>();
        int dungeonCounter = 9;
        int questsCounter = 9;
        int grindingCounter = 9;
        int minibossCounter = 9;
        dungeon.setItem(4, GroupPurpose.Type.DUNGEON.getIcon());
        quests.setItem(4, GroupPurpose.Type.QUESTS.getIcon());
        grinding.setItem(4, GroupPurpose.Type.GRINDING.getIcon());
        miniboss.setItem(4, GroupPurpose.Type.MINIBOSS.getIcon());
        for (GroupPurpose purpose : GroupPurpose.values()) {
            switch (purpose.getType()) {
                case DUNGEON:
                    dungeon.setItem(dungeonCounter, purpose.getIcon());
                    dungeonSlots.put(dungeonCounter, purpose);
                    dungeonCounter++;
                    break;
                case QUESTS:
                    quests.setItem(questsCounter, purpose.getIcon());
                    questsSlots.put(questsCounter, purpose);
                    questsCounter++;
                    break;
                case GRINDING:
                    grinding.setItem(grindingCounter, purpose.getIcon());
                    grindingSlots.put(grindingCounter, purpose);
                    grindingCounter++;
                    break;
                case MINIBOSS:
                    miniboss.setItem(minibossCounter, purpose.getIcon());
                    minibossSlots.put(minibossCounter, purpose);
                    minibossCounter++;
                    break;
                default:
                    break;
            }
        }
        inventories.put(GroupPurpose.Type.DUNGEON, new PurposeInventory(dungeon, dungeonSlots));
        inventories.put(GroupPurpose.Type.QUESTS, new PurposeInventory(quests, questsSlots));
        inventories.put(GroupPurpose.Type.GRINDING, new PurposeInventory(grinding, grindingSlots));
        inventories.put(GroupPurpose.Type.MINIBOSS, new PurposeInventory(miniboss, minibossSlots));
    }

    public static void display(Player player, GroupPurpose.Type type) {
        player.openInventory(inventories.get(type).getInventory());
        viewers.put(player, type);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (viewers.containsKey(player)) {
                event.setCancelled(true);
                if (event.getRawSlot() < event.getInventory().getSize()) {
                    if (inventories.get(viewers.get(player)).getSlots().containsKey(event.getSlot())) {
                        if (RunicCore.getGroupManager().canJoinGroup(player)) {
                            // TODO - create group
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

    private static class PurposeInventory {

        private Inventory inventory;
        private Map<Integer, GroupPurpose> slots;

        public PurposeInventory(Inventory inventory, Map<Integer, GroupPurpose> slots) {
            this.inventory = inventory;
            this.slots = slots;
        }

        public Inventory getInventory() {
            return this.inventory;
        }

        public Map<Integer, GroupPurpose> getSlots() {
            return this.slots;
        }

    }

}