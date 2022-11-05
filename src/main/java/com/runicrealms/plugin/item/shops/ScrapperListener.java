package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class ScrapperListener implements Listener {

    /**
     * Handles logic for the shop menus
     */
    @EventHandler
    public void onShopClick(InventoryClickEvent e) {

                /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof ItemScrapper.ItemScrapperHolder)) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) return;
        ItemScrapper.ItemScrapperHolder itemScrapperHolder = (ItemScrapper.ItemScrapperHolder) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(itemScrapperHolder.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (itemScrapperHolder.getInventory().getItem(e.getRawSlot()) == null) return;
        ItemScrapper itemScrapper = (ItemScrapper) RunicCore.getRunicShopManager().getPlayersInShops().get(player.getUniqueId());

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        if (!ItemScrapper.SCRAPPER_SLOTS.contains(e.getRawSlot())) { // e.getClickedInventory().equals(e.getView().getTopInventory()) &&
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            e.setCancelled(true);
        }

        if (material == GUIUtil.CLOSE_BUTTON.getType())
            player.closeInventory();
        else if (material == ItemScrapper.checkMark().getType())
            itemScrapper.scrapItems(player);
    }

    /**
     * Drop items to prevent item loss from shops
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onShopClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        if (RunicCore.getRunicShopManager().getPlayersInShops().get(player.getUniqueId()) == null) return;
        RunicShop shop = RunicCore.getRunicShopManager().getPlayersInShops().get(player.getUniqueId());
        String title = ChatColor.translateAlternateColorCodes('&', shop.getName());
        if (shop instanceof ItemScrapper && title.equals(e.getView().getTitle())) {
            for (Integer slot : ItemScrapper.SCRAPPER_SLOTS) {
                ItemStack itemStack = e.getInventory().getItem(slot);
                if (itemStack == null) continue;
                if (((ItemScrapper) shop).getStoredItems().get(player.getUniqueId()).contains(itemStack)) continue;
                RunicItemsAPI.addItem(player.getInventory(), itemStack, player.getLocation());
            }
            RunicCore.getRunicShopManager().getPlayersInShops().remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onNpcClick(NpcClickEvent e) {
        if (!ItemScrapper.SCRAPPER_NPC_IDS.contains(e.getNpc().getId())) return;
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        ItemScrapper itemScrapper = new ItemScrapper(e.getPlayer());
        RunicCore.getRunicShopManager().getPlayersInShops().put(e.getPlayer().getUniqueId(), itemScrapper);
        e.getPlayer().openInventory(itemScrapper.getInventoryHolder().getInventory());
    }
}
