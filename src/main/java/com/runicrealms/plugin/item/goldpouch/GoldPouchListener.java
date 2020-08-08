package com.runicrealms.plugin.item.goldpouch;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class GoldPouchListener implements Listener {

    /*
    Place picked up coins in pouch automatically
     */
    @EventHandler
    public void onCoinPickup(PlayerAttemptPickupItemEvent e) {

        if (e.getItem().getItemStack().getType() != Material.GOLD_NUGGET) return;
        Inventory inv = e.getPlayer().getInventory();
        for (ItemStack is : inv) {
            if (is != null && is.getItemMeta() != null) {
                if (is.getType() == Material.SHEARS && ((Damageable) is.getItemMeta()).getDamage() == 234) {

                    // if pouch isnt full
                    int maxAmount = (int) AttributeUtil.getCustomDouble(is, "pouchSize");
                    int currentAmount = (int) AttributeUtil.getCustomDouble(is, "goldAmount");
                    if (currentAmount >= maxAmount) continue;

                    // update pouch w/ amount, remove coins on floor
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    // place gold coins in pouch, remove coins on floor
                    int leftOverCoins = placeGoldInPouch(e.getPlayer(), is, e.getItem().getItemStack());
                    e.setCancelled(true);
                    e.getItem().remove();
                    if (leftOverCoins > 0) {
                        HashMap<Integer, ItemStack> coinsToAdd = e.getPlayer().getInventory().addItem(CurrencyUtil.goldCoin(leftOverCoins));
                        for (ItemStack coin : coinsToAdd.values()) {
                            e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), coin);
                        }
                    }
                    return;
                }
            }
        }
    }

    /*
    Empties the pouch when a player right-clicks
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta() == null) return;
        if (!(e.getItem().getType() == Material.SHEARS && ((Damageable) e.getItem().getItemMeta()).getDamage() == 234))
            return;

        Player pl = e.getPlayer();
        ItemStack pouch = e.getItem();
        int maxAmount = (int) AttributeUtil.getCustomDouble(pouch, "pouchSize");
        int currentAmount = (int) AttributeUtil.getCustomDouble(pouch, "goldAmount");

        if (currentAmount == 0)
            return;

        e.setCancelled(true);

        // see how many in pouch, remove old pouch
        pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.5f, 1.0f);

        // give new pouch
        ItemStack emptyPouch = new ItemStack(Material.SHEARS);
        emptyPouch = AttributeUtil.addCustomStat(emptyPouch, "pouchSize", maxAmount);
        LoreGenerator.generateGoldPouchLore(emptyPouch);
        ItemRemover.takeItem(pl, pouch, 1);

        // give coins contained inside
        HashMap<Integer, ItemStack> pouchToAdd = pl.getInventory().addItem(emptyPouch);
        for (ItemStack is : pouchToAdd.values()) {
            pl.getWorld().dropItem(pl.getLocation(), is);
        }

        // give coins contained inside
        HashMap<Integer, ItemStack> coinsToAdd = pl.getInventory().addItem(CurrencyUtil.goldCoin(currentAmount));
        for (ItemStack is : coinsToAdd.values()) {
            pl.getWorld().dropItem(pl.getLocation(), is);
        }
    }

    /*
    When a player clicks on a gold pouch
     */
    @EventHandler
    public void onClick(InventoryClickEvent e) {

        // item must be gold pouch
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        if (e.getCursor() == null) return;
        if (!(e.getCurrentItem().getType() == Material.SHEARS && ((Damageable) e.getCurrentItem().getItemMeta()).getDamage() == 234))
            return;

        ItemStack pouch = e.getCurrentItem();
        int maxAmount = (int) AttributeUtil.getCustomDouble(pouch, "pouchSize");
        int currentAmount = (int) AttributeUtil.getCustomDouble(pouch, "goldAmount");

        if (e.getCursor().getType() != Material.GOLD_NUGGET) return;

        e.setCancelled(true);

        // return if limit is met
        if (currentAmount >= maxAmount) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.GRAY + "The pouch is full.");
            return;
        }

        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);

        // take pouch, give new pouch, take coins, distribute leftover coins
        int leftOverCoins = placeGoldInPouch(pl, pouch, e.getCursor());
        e.setCursor(new ItemStack(Material.AIR));
        if (leftOverCoins > 0) {
            HashMap<Integer, ItemStack> coinsToAdd = pl.getInventory().addItem(CurrencyUtil.goldCoin(leftOverCoins));
            for (ItemStack is : coinsToAdd.values()) {
                pl.getWorld().dropItem(pl.getLocation(), is);
            }
        }
    }

    /**
     * Adds gold coins to a pouch.
     * @param goldPouch the item to modify
     * @param coins on the cursor
     * @return leftOver, the amount of coins to add to player's inv if the pouch is full.
     */
    private int placeGoldInPouch(Player pl, ItemStack goldPouch, ItemStack coins) {

        int maxAmount = (int) AttributeUtil.getCustomDouble(goldPouch, "pouchSize");
        int currentAmount = (int) AttributeUtil.getCustomDouble(goldPouch, "goldAmount");
        int newAmount = currentAmount + coins.getAmount();
        int leftOver = 0;

        if (newAmount > maxAmount) {
            leftOver = newAmount - maxAmount;
            newAmount = maxAmount;
        }

        // give them the new pouch
        ItemRemover.takeItem(pl, goldPouch, 1);
        goldPouch = AttributeUtil.addCustomStat(goldPouch, "goldAmount", newAmount);
        LoreGenerator.generateGoldPouchLore(goldPouch);
        HashMap<Integer, ItemStack> pouchToAdd = pl.getInventory().addItem(goldPouch);
        for (ItemStack is : pouchToAdd.values()) {
            pl.getWorld().dropItem(pl.getLocation(), is);
        }

        return leftOver;
    }
}
