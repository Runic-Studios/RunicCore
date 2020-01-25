package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.scrapper.ItemScrapper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class SoulboundListener implements Listener {

    /**
     * Disables use of soulbound items in shop screens
     */
    @EventHandler
    public void onSoulboundItemClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getItem(e.getRawSlot()) == null) return;
        Player pl = (Player) e.getWhoClicked();
        ItemScrapper shop = (ItemScrapper) RunicCore.getShopManager().getPlayerShop(pl);
        String title = ChatColor.translateAlternateColorCodes('&', shop.getTitle());
        if (title.equals(e.getInventory().getTitle())) { // verify custom GUI
            ItemStack item = e.getCurrentItem();
            String soulbound = AttributeUtil.getCustomString(item, "soulbound");
            if (soulbound.equals("true")) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {

        Player pl = e.getPlayer();
        boolean isSoulbound = false;
        String soulbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound");
        if (soulbound.equals("true")) {
            isSoulbound = true;
        }

        if (isSoulbound && pl.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
