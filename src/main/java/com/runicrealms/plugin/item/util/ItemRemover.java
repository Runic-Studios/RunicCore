package com.runicrealms.plugin.item.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemRemover {

    // todo: gold pouch calculator?
    public static void takeItem(Player pl, ItemStack itemStack, int amount) {

        int to_take = amount;
        for (ItemStack player_item : pl.getInventory().getContents()) {
            if (player_item != null) {

                if (player_item.equals(itemStack)) {
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
    private static void remove(Player p, ItemStack toR, int amount){
        ItemStack i = toR.clone();
        i.setAmount(amount);
        p.getInventory().removeItem(i);
    }
}
