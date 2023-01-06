package com.runicrealms.plugin.item.lootchests;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum LootChestTier {

    TIER_I("common", "Tier I Chest", ChatColor.GRAY, Color.WHITE, 0, 1, 9, 2, 4, 600), // 10 min
    TIER_II("uncommon", "Tier II Chest", ChatColor.GREEN, Color.LIME, 10, 10, 24, 2, 4, 900), // 15 min
    TIER_III("rare", "Tier III Chest", ChatColor.AQUA, Color.AQUA, 25, 25, 39, 3, 5, 1200), // 20 min
    TIER_IV("epic", "Tier IV Chest", ChatColor.LIGHT_PURPLE, Color.FUCHSIA, 40, 40, 60, 3, 6, 2700); // 45 min

    private final String identifier;
    private final String title;
    private final ChatColor chatColor;
    private final Color color;
    private final int minAccessLevel;
    private final int minLootLevel;
    private final int maxLootLevel;
    private final int minimumItems;
    private final int maximumItems;
    private final int respawnTimeSeconds;

    /**
     * Enumerates a set of loot chest rarities which determine the content of the box
     *
     * @param identifier         the string name of the rarity e.g., common
     * @param title              the title of the chest ui
     * @param chatColor          the chat color associated with the rarity
     * @param color              the color associated with the rarity
     * @param minAccessLevel     the minimum level to open the box
     * @param minLootLevel       the minimum level of the item loot
     * @param maxLootLevel       the max level of the item loot
     * @param minimumItems       the min number of items in the box
     * @param maximumItems       the max number of items in the box
     * @param respawnTimeSeconds how long before boxes of this rarity respawn (shared timer)
     */
    LootChestTier(String identifier, String title, ChatColor chatColor,
                  Color color, int minAccessLevel, int minLootLevel, int maxLootLevel,
                  int minimumItems, int maximumItems, int respawnTimeSeconds) {
        this.identifier = identifier;
        this.title = title;
        this.chatColor = chatColor;
        this.color = color;
        this.minAccessLevel = minAccessLevel;
        this.minLootLevel = minLootLevel;
        this.maxLootLevel = maxLootLevel;
        this.minimumItems = minimumItems;
        this.maximumItems = maximumItems;
        this.respawnTimeSeconds = respawnTimeSeconds;
    }

    /**
     * Returns an enum based on a string identifier
     *
     * @param identifier the name of the rarity
     * @return an enum
     */
    public static LootChestTier getFromIdentifier(String identifier) {
        for (LootChestTier lootChestTier : LootChestTier.values()) {
            if (lootChestTier.getIdentifier().equals(identifier))
                return lootChestTier;
        }
        return null;
    }

    public Color getColor() {
        return color;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getMaxLootLevel() {
        return maxLootLevel;
    }

    public int getMaximumItems() {
        return maximumItems;
    }

    public int getMinAccessLevel() {
        return minAccessLevel;
    }

    public int getMinLootLevel() {
        return minLootLevel;
    }

    public int getMinimumItems() {
        return minimumItems;
    }

    public int getRespawnTimeSeconds() {
        return respawnTimeSeconds;
    }

    public String getTitle() {
        return this.chatColor + "" + ChatColor.BOLD + title;
    }
}
