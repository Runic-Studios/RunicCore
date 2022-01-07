package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class GroupFinderMiniBossUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Map<Integer, GroupManager.QueueReason> reasons;

    public GroupFinderMiniBossUI() {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&r&aGroup Finder"));
        this.reasons = new HashMap<>();
        this.inventory.setItem(0, GUIUtil.backButton());
        GUIUtil.fillInventoryBorders(this.getInventory());

        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&r"));
        item.setItemMeta(meta);

        for (GroupManager.QueueReason reason : GroupManager.REASONS) {
            if (reason instanceof GroupManager.MiniBosses) {
                ItemStack icon = item.clone();
                ItemMeta iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(reason.getItemName());
                icon.setItemMeta(iconMeta);
                int slot = this.getInventory().firstEmpty();
                this.inventory.setItem(slot, icon);
                this.reasons.put(slot, reason);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory().getHolder() instanceof GroupFinderMiniBossUI)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        // back
        if (item.getType() == GUIUtil.backButton().getType()) {
            player.openInventory(RunicCore.getGroupManager().getUI().getInventory());
            return;
        }

        int slot = event.getRawSlot();
        if (!(this.reasons.containsKey(slot))) return;
        GroupManager.QueueReason reason = this.reasons.get(slot);
        PlayerCache cache = RunicCore.getCacheManager().getPlayerCaches().get(player);

        if (cache.getClassLevel() >= reason.getMinLevel()) {
            RunicCore.getGroupManager().addToQueue(reason, player);
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&r&aYou have been added into the queue!"));
        } else {
            player.sendMessage(ColorUtil.format("&r&cYou are not high enough level to be added into the queue!"));
        }
    }
}
