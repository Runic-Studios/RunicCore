package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.runicnpcs.api.NpcClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunicShopManager implements Listener {

    private static final Map<Integer, RunicItemShop> shops = new HashMap<>();
    private static final Map<UUID, Long> clickCooldowns = new HashMap<>();
    private static final Map<UUID, RunicItemShop> inShop = new HashMap<>();
    private static ItemStack blankSlot;

    public static void registerShop(RunicItemShop shop) {
        for (Integer npc : shop.getNpcIds()) {
            shops.put(npc, shop);
        }
        if (blankSlot == null) {
            blankSlot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = blankSlot.getItemMeta();
            if (meta == null) return;
            meta.setDisplayName(" ");
            blankSlot.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        if (clickCooldowns.containsKey(event.getPlayer().getUniqueId())) {
            if (clickCooldowns.get(event.getPlayer().getUniqueId()) + 2000 > System.currentTimeMillis()) {
                return;
            }
        }
        clickCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (shops.containsKey(event.getNpc().getId())) {
            RunicItemShop shop = shops.get(event.getNpc().getId());
            Inventory inventory = Bukkit.createInventory(null, 9 + shop.getShopSize(), shop.getName());
            for (int i = 0; i < 9; i++) {
                if (i != 4) {
                    inventory.setItem(i, blankSlot);
                }
            }
            inventory.setItem(4, shop.getIcon());
            for (Map.Entry<Integer, RunicShopItem> trade : shop.getContents().entrySet()) {
                inventory.setItem(trade.getKey() + 9, trade.getValue().getItem());
            }
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            event.getPlayer().openInventory(inventory);
            inShop.put(event.getPlayer().getUniqueId(), shop);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (inShop.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
                if (inShop.get(player.getUniqueId()).getContents().containsKey(event.getSlot() - 9)) {
                    if (event.getRawSlot() < event.getInventory().getSize()) {
                        RunicShopItem item = inShop.get(player.getUniqueId()).getContents().get(event.getSlot() - 9);
                        if (player.getInventory().firstEmpty() != -1
                                && hasItems(player, item.getRunicItemCurrency(), item.getPrice())) {
                            if (item.removePayment()) {
                                ItemRemover.takeItem(player, item.getRunicItemCurrency(), item.getPrice());
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                player.updateInventory();
                            }
                            player.closeInventory();
                            item.runBuy(player);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou purchased this item!"));
                        } else {
                            player.closeInventory();
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                            if (player.getInventory().firstEmpty() == -1)
                                player.sendMessage(ChatColor.RED + "You don't have enough inventory to buy this!");
                            else
                                player.sendMessage(ChatColor.RED + "You don't have enough items to buy this!");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        inShop.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clickCooldowns.remove(event.getPlayer().getUniqueId());
        inShop.remove(event.getPlayer().getUniqueId());
    }

    private static boolean hasItems(Player player, ItemStack item, Integer needed) {
        int amount = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null) {
                if (inventoryItem.isSimilar(item)) {
                    amount += inventoryItem.getAmount();
                    if (amount >= needed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
