package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GroupFinderMainUI implements InventoryHolder, Listener {

    private final Inventory inventory;

    public GroupFinderMainUI() {
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.format("&r&aGroup Finder"));
        this.inventory.setItem(11, miniBoss());
        this.inventory.setItem(13, grinding());
        this.inventory.setItem(15, dungeons());
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory().getHolder() instanceof GroupFinderMainUI)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        event.setCancelled(true);
        HumanEntity player = event.getWhoClicked();

        Material material = item.getType();

        if (material == miniBoss().getType()) {
            player.openInventory(RunicCore.getGroupManager().getMiniBossUI().getInventory());
        } else if (material == grinding().getType()) {
            player.openInventory(RunicCore.getGroupManager().getGrindingUI().getInventory());
        } else if (material == dungeons().getType()) {
            player.openInventory(RunicCore.getGroupManager().getDungeonUI().getInventory());
        }
    }

    private ItemStack miniBoss() {
        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&r&6Mini-Bosses"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack grinding() {
        ItemStack item = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&r&6Grinding"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack dungeons() {
        ItemStack item = new ItemStack(Material.ENDER_EYE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&r&6Dungeons"));
        item.setItemMeta(meta);
        return item;
    }
}
