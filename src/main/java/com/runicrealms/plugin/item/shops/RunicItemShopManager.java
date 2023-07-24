package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.NpcClickEvent;
import com.runicrealms.plugin.api.ShopAPI;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.config.ShopConfigLoader;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.util.ItemUtils;
import com.runicrealms.runicitems.weaponskin.WeaponSkinObtainEvent;
import com.runicrealms.runicitems.weaponskin.WeaponSkinUtil;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RunicItemShopManager implements Listener, ShopAPI {
    private static final int LOAD_DELAY = 10; // to allow RunicItems to load
    private static final Map<Integer, RunicItemShop> shops = new HashMap<>();
    private static final Map<UUID, Long> clickCooldowns = new HashMap<>();
    private static final Map<UUID, RunicItemShop> inShop = new HashMap<>();
    private static ItemStack blankSlot;

    /**
     * Loads shops into memory on a delay to allow RunicItems to load
     */
    public RunicItemShopManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            Bukkit.getLogger().info("RunicCore is loading all shops!");
            new RunicItemShopHelper();
            ShopConfigLoader.init(); // load shops from yaml storage
        }, LOAD_DELAY * 20L);
    }

    @Override
    public boolean checkItemRequirement(Player player, List<Pair<String, Integer>> requiredItems,
                                        String itemDisplayName, boolean removePayment) {
        if (player.getInventory().firstEmpty() == -1) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough inventory space to buy this!");
            return false;
        }
        if (!hasAllReqItems(player, requiredItems)) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough items to buy this!");
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getRunicItemCurrency(String templateID) {
        return RunicItemsAPI.generateItemFromTemplate(templateID).generateGUIItem();
    }

    @Override
    public boolean hasAllReqItems(Player player, List<Pair<String, Integer>> requiredItems) {
        for (Pair<String, Integer> pair : requiredItems) {
            ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(pair.first).generateItem();
            if (!hasItem(player, itemStack, pair.second))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasItem(Player player, ItemStack itemStack, int needed) {
        if (needed == 0) return true;
        int amount = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null) {
                if (RunicItemsAPI.isRunicItemSimilar(itemStack, inventoryItem)) {
                    amount += inventoryItem.getAmount();
                    if (amount >= needed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void registerRunicItemShop(RunicItemShop shop) {
        for (Integer npc : shop.getRunicNpcIds()) {
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!inShop.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
        if (!inShop.get(player.getUniqueId()).getContents().containsKey(event.getSlot() - 9))
            return;
        if (event.getRawSlot() > event.getInventory().getSize()) return;
        RunicShopItem runicShopItem = inShop.get(player.getUniqueId()).getContents().get(event.getSlot() - 9);
        String displayName = runicShopItem.getShopItem().getItemMeta() != null ?
                runicShopItem.getShopItem().getItemMeta().getDisplayName() : "Item";
        boolean requirementsMet = checkItemRequirement(player, runicShopItem.getRequiredItems(),
                displayName, runicShopItem.removePayment());
        List<ShopCondition> extraConditions = runicShopItem.getExtraConditions();
        for (ShopCondition condition : extraConditions) {
            if (!condition.test(player)) {
                requirementsMet = false;
                break;
            }
        }
        if (!requirementsMet) return;
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        if (runicShopItem.removePayment() && !runicShopItem.isFree()) {
            for (Pair<String, Integer> pair : runicShopItem.getRequiredItems()) {
                ItemUtils.takeItem
                        (
                                player,
                                this.getRunicItemCurrency(pair.first),
                                pair.second
                        );
            }
            player.updateInventory();
            player.sendMessage(ChatColor.GREEN + "You purchased " + displayName + ChatColor.GREEN + "!");
        }
        runicShopItem.runBuy(player);
        WeaponSkinObtainEvent obtainEvent = WeaponSkinUtil.createObtainEvent(player, runicShopItem.getShopItem());
        if (obtainEvent != null) Bukkit.getPluginManager().callEvent(obtainEvent);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        inShop.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onNpcClick(NpcClickEvent event) {
        if (event.isCancelled()) return;
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
            for (Map.Entry<Integer, RunicShopItem> runicShopItemEntry : shop.getContents().entrySet()) {
                inventory.setItem
                        (
                                runicShopItemEntry.getKey() + 9,
                                RunicShopItem.iconWithLore(runicShopItemEntry.getValue())
                        );
            }
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            event.getPlayer().openInventory(inventory);
            inShop.put(event.getPlayer().getUniqueId(), shop);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clickCooldowns.remove(event.getPlayer().getUniqueId());
        inShop.remove(event.getPlayer().getUniqueId());
    }

}
