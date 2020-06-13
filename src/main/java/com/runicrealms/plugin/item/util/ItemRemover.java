package com.runicrealms.plugin.item.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemRemover {

    /**
     * Removes item based on object.
     */
    public static void takeItem(Player pl, ItemStack itemStack, int amount) {
        int to_take = amount;

        if (pl.getInventory().getItemInOffHand().isSimilar(itemStack)) {
            Bukkit.broadcastMessage("here");
            int take_next = Math.min(to_take, pl.getInventory().getItemInOffHand().getAmount());
            remove(pl, pl.getInventory().getItemInOffHand(), take_next);
            to_take -= take_next;
            if (to_take <= 0) { //Reached amount. Can stop!
                return;
            }
        }

        for (ItemStack player_item : pl.getInventory().getContents()) {
            if (player_item != null) {

                if (player_item.isSimilar(itemStack)) {
                    int take_next = Math.min(to_take, player_item.getAmount());
                    remove(pl, player_item, take_next);
                    to_take -= take_next;
                    if (to_take <= 0) { //Reached amount. Can stop!
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes item based on material
     */
    public static void takeItem(Player pl, Material material, int amount) {
        int to_take = amount;
        for (ItemStack player_item : pl.getInventory().getContents()) {
            if (player_item != null) {

                if (player_item.getType() == material) {
                    int take_next = Math.min(to_take, player_item.getAmount());
                    remove(pl, player_item, take_next);
                    to_take -= take_next;
                    if (to_take <= 0) { //Reached amount. Can stop!
                        break;
                    }
                }
            }
        }
    }

    private static void remove(Player p, ItemStack toR, int amount) {
        ItemStack i = toR.clone();
        i.setAmount(amount);
        p.getInventory().removeItem(i);
    }
}
