package com.runicrealms.plugin.api;

import com.runicrealms.plugin.loot.LootChestTemplate;
import com.runicrealms.plugin.loot.LootTable;
import com.runicrealms.plugin.loot.RegenerativeLootChest;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface LootAPI {

    /**
     * Get a loot table based off of its identifier.
     * These can define different type of loot that exists in chests templates.
     */
    LootTable getLootTable(String identifier);

    /**
     * Get a loot chest template based off of its identifier.
     * These define different types of physical chests that exist in the world.
     */
    LootChestTemplate getLootChestTemplate(String identifier);

    /**
     * Gets a loot chest based off of its location.
     * These are specifically the loot chests placed around the world and not dungeon/field-boss loot chests.
     */
    @Nullable
    RegenerativeLootChest getRegenerativeLootChest(Location location);

    /**
     * Adds a world loot chest to the configuration file.
     * Regeneration time is in seconds.
     */
    void addRegenerativeLootChestToConfig(RegenerativeLootChest regenerativeLootChest);

    /**
     * Removes a world loot chest from the configuration file.
     */
    void removeRegenerativeLootChestFromConfig(RegenerativeLootChest regenerativeLootChest);


    /**
     * Checks if the given loot chest template identifier exists
     */
    boolean isLootChestTemplate(String identifier);

    /**
     * Gets all possible chest templates.
     */
    Collection<LootChestTemplate> getChestTemplates();

    /**
     * Gets all possible regenerative loot chests.
     */
    Collection<RegenerativeLootChest> getRegenerativeLootChests();

}