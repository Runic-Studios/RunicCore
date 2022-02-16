package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.GUIMenu.OptionClickEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ScrapperListener implements Listener {

    /**
     * Handles logic for the shop menus
     */
    @EventHandler
    public void onShopClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        UUID uuid = player.getUniqueId();
        if (RunicCore.getRunicShopManager().getPlayersInShops().get(uuid) == null) return;
        if (!(RunicCore.getRunicShopManager().getPlayersInShops().get(uuid) instanceof ItemScrapper)) return;
        ItemScrapper shop = (ItemScrapper) RunicCore.getRunicShopManager().getPlayersInShops().get(uuid);
        String title = ChatColor.translateAlternateColorCodes('&', shop.getName());
        if (!title.equals(e.getView().getTitle())) return; // verify custom GUI

        int slot = e.getRawSlot();

        // shop gui
        if (e.getClickedInventory() == null) return;
        if (!e.getView().getTitle().equals(title)) return;
        if (e.getClickedInventory().equals(e.getView().getBottomInventory())) return;

        if (slot == 16 || slot == 17 || !ItemScrapper.SCRAPPER_SLOTS.contains(slot)) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }

        /*
        Call custom ItemGUI logic
         */
        if (shop.getItemGUI() == null) return;
        ItemGUI itemGUI = shop.getItemGUI();
        if (slot >= 0 && slot < itemGUI.getSize() && itemGUI.getOptionNames()[slot] != null) {

            OptionClickEvent ope = new OptionClickEvent(e, (Player) e.getWhoClicked(), slot, itemGUI.getOptionNames()[slot]);
            itemGUI.getHandler().onOptionClick(ope);

            if (ope.willClose()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), player::closeInventory, 1);
            }
            if (ope.willDestroy()) {
                itemGUI.destroy();
            }
        }

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
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onNpcClick(NpcClickEvent e) {
        if (!ItemScrapper.SCRAPPER_NPC_IDS.contains(e.getNpc().getId())) return;
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        ItemScrapper itemScrapper = new ItemScrapper(e.getPlayer());
        RunicCore.getRunicShopManager().getPlayersInShops().put(e.getPlayer().getUniqueId(), itemScrapper);
        itemScrapper.getItemGUI().open(e.getPlayer());
    }
}
