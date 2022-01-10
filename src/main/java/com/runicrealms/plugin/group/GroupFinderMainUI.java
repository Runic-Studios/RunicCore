package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupFinderMainUI implements InventoryHolder, Listener {

    private static final int INVENTORY_SIZE = 54;
    private final Inventory inventory;

    public GroupFinderMainUI() {
        this.inventory = Bukkit.createInventory(this, INVENTORY_SIZE, ColorUtil.format("&r&aGroup Finder"));
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.closeButton());
        this.inventory.setItem(4, infoItem());
        this.inventory.setItem(20, miniBoss());
        this.inventory.setItem(22, grinding());
        this.inventory.setItem(24, dungeons());
    }

    @NotNull
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

        if (material == GUIUtil.closeButton().getType()) {
            player.closeInventory();
        } else if (material == miniBoss().getType()) {
            player.openInventory(RunicCore.getGroupManager().getMiniBossUI().getInventory());
        } else if (material == grinding().getType()) {
            player.openInventory(RunicCore.getGroupManager().getGrindingUI().getInventory());
        } else if (material == dungeons().getType()) {
            player.openInventory(RunicCore.getGroupManager().getDungeonUI().getInventory());
        }
    }

    private ItemStack infoItem() {
        ItemStack item = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&cGroup Finder"));
        List<String> idk = ChatUtils.formattedText("&7The group finder can help you find a group of players to tackle challenging monsters!");
        meta.setLore(idk);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack miniBoss() {
        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&r&6Mini-Bosses"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack grinding() {
        ItemStack item = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&r&6Grinding"));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack dungeons() {
        ItemStack item = new ItemStack(Material.ENDER_EYE, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&r&6Dungeons"));
        item.setItemMeta(meta);
        return item;
    }
}
