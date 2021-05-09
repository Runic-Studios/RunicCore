package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.utilities.ColorUtil;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupFinderDungeonUI implements InventoryHolder, Listener {
    private final Inventory inventory;
    private final Map<Integer, GroupManager.QueueReason> reasons;

    public GroupFinderDungeonUI() {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&r&aGroup Finder"));
        this.reasons = new HashMap<>();

        this.inventory.setItem(0, this.back());

        ItemStack background = this.background();
        int[] slots = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48,
                49, 50, 51, 52, 53};
        for (int slot : slots) {
            this.inventory.setItem(slot, background);
        }

        ItemStack item = new ItemStack(Material.IRON_BARS, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&r"));
        item.setItemMeta(meta);

        for (GroupManager.QueueReason reason : GroupManager.REASONS) {
            if (reason instanceof GroupManager.Dungeons) {
                ItemStack icon = item.clone();
                ItemMeta iconMeta = icon.getItemMeta();
                iconMeta.setDisplayName(reason.getItemName());
                icon.setItemMeta(iconMeta);
                int slot = this.inventory.firstEmpty();
                this.inventory.setItem(slot,
                        getHead(((GroupManager.Dungeons) reason).getSkullPlayerName(),
                                reason.getItemName(),
                                ((GroupManager.Dungeons) reason).getItemDescription()));
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
        if (!(event.getView().getTopInventory().getHolder() instanceof GroupFinderDungeonUI)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        if (item.getType() == Material.ARROW) {
            //back
            player.closeInventory();
            player.openInventory(RunicCore.getGroupManager().getUI().getInventory());
            return;
        }

        int slot = event.getRawSlot();

        if (!(this.reasons.containsKey(slot))) {
            return;
        }

        GroupManager.QueueReason reason = this.reasons.get(slot);

        PlayerCache cache = RunicCoreAPI.getPlayerCache(player);

        if (cache.getClassLevel() >= reason.getMinLevel()) {
            RunicCore.getGroupManager().addToQueue(reason, player);
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&r&aYou have been added into the queue!"));
        } else {
            player.sendMessage(ColorUtil.format("&r&cYou are not high enough level to be added into the queue!"));
        }
    }

    private ItemStack background() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack back() {
        ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtil.format("&r&fBack"));

        item.setItemMeta(meta);
        return item;
    }

    /**
     *
     * @param playerName
     * @param displayName
     * @param itemDescription
     * @return
     */
    private static ItemStack getHead(String playerName, String displayName, String[] itemDescription) {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(displayName);
        ArrayList<String> lore = new ArrayList<>();
        for (String s : itemDescription) {
            lore.add(ColorUtil.format(s));
        }
        skull.setLore(lore);
        skull.setOwner(playerName);
        item.setItemMeta(skull);
        return item;
    }
}
