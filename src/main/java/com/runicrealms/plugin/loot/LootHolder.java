package com.runicrealms.plugin.loot;

import org.bukkit.entity.Player;

public interface LootHolder {

    /**
     * Gets the min level of items that a loot holder should generate for a given player.
     * This is currently used exclusively for script item generation.
     */
    int getItemMinLevel(Player player);

    /**
     * Gets the max level of items that a loot holder should generate for a given player.
     * This is currently used exclusively for script item generation.
     */

    int getItemMaxLevel(Player player);

}
