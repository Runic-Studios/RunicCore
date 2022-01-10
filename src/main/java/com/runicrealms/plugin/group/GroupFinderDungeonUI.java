package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GroupFinderDungeonUI implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final Map<Integer, GroupFinderItem> groupFinderItems; // keeps track of the inventory slot matching the item

    public GroupFinderDungeonUI() {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&r&aGroup Finder"));
        this.groupFinderItems = new HashMap<>();
        this.inventory.setItem(0, GUIUtil.backButton());
        GUIUtil.fillInventoryBorders(this.getInventory());
        for (GroupFinderItem groupFinderItem : GroupFinderItem.values()) {
            if (groupFinderItem.getQueueReason() != QueueReason.DUNGEONS) continue;
            int slot = this.getInventory().firstEmpty();
            this.inventory.setItem(slot, groupFinderItem.getMenuItem());
            this.groupFinderItems.put(slot, groupFinderItem);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!(e.getView().getTopInventory().getHolder() instanceof GroupFinderDungeonUI)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();

        // back
        if (item.getType() == GUIUtil.backButton().getType()) {
            player.openInventory(RunicCore.getGroupManager().getUI().getInventory());
            return;
        }

        int slot = e.getRawSlot();
        if (!(this.groupFinderItems.containsKey(slot))) return;
        GroupFinderItem groupFinderItem = this.groupFinderItems.get(slot);
        PlayerCache cache = RunicCoreAPI.getPlayerCache(player);

        if (cache.getClassLevel() >= groupFinderItem.getMinLevel()) {
            RunicCore.getGroupManager().addToQueue(groupFinderItem, player);
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&r&aYou have been added into the queue!"));
        } else {
            player.sendMessage(ColorUtil.format("&r&cYou are not high enough level to be added into the queue!"));
        }
    }
}
