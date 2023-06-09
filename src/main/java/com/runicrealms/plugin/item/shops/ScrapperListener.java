package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
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

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onNpcClick(NpcClickEvent event) {
        if (!ItemScrapper.SCRAPPER_NPC_IDS.contains(event.getNpc().getId())) return;
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        ItemScrapper itemScrapper = new ItemScrapper(event.getPlayer());
        RunicCore.getRunicShopManager().getPlayersInShops().put(event.getPlayer().getUniqueId(), itemScrapper);
        event.getPlayer().openInventory(itemScrapper.getInventoryHolder().getInventory());
    }

    /**
     * Handles logic for the shop menus
     */
    @EventHandler
    public void onShopClick(InventoryClickEvent event) {

                /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof ItemScrapper.ItemScrapperHolder)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;
        ItemScrapper.ItemScrapperHolder itemScrapperHolder = (ItemScrapper.ItemScrapperHolder) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(itemScrapperHolder.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (itemScrapperHolder.getInventory().getItem(event.getRawSlot()) == null) return;
        ItemScrapper itemScrapper = (ItemScrapper) RunicCore.getRunicShopManager().getPlayersInShops().get(player.getUniqueId());

        if (itemScrapper == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        if (!ItemScrapper.SCRAPPER_SLOTS.contains(event.getRawSlot())) { // e.getClickedInventory().equals(e.getView().getTopInventory()) &&
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            event.setCancelled(true);
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
}
