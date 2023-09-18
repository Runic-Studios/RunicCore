package com.runicrealms.plugin.api;

import com.runicrealms.plugin.loot.LootTable;
import com.runicrealms.plugin.loot.chest.BossTimedLoot;
import com.runicrealms.plugin.loot.chest.CustomTimedLoot;
import com.runicrealms.plugin.loot.chest.LootChestTemplate;
import com.runicrealms.plugin.loot.chest.RegenerativeLootChest;
import com.runicrealms.plugin.loot.chest.TimedLootChest;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface LootAPI {

    /**
     * Get a loot table based off of its identifier.
     * These can define different type of loot that exists in chests templates.
     */
    @Nullable
    LootTable getLootTable(@NotNull String identifier);

    /**
     * Get a loot chest template based off of its identifier.
     * These define different types of physical chests that exist in the world.
     */
    @Nullable
    LootChestTemplate getLootChestTemplate(@NotNull String identifier);

    /**
     * Gets a loot chest based off of its location.
     * These are specifically the loot chests placed around the world and not dungeon/field-boss loot chests.
     */
    @Nullable
    RegenerativeLootChest getRegenerativeLootChest(@NotNull Location location);

    /**
     * Adds a world loot chest to the configuration file.
     * Regeneration time is in seconds.
     */
    void createRegenerativeLootChest(@NotNull RegenerativeLootChest regenerativeLootChest);

    /**
     * Removes a world loot chest from the configuration file.
     */
    void deleteRegenerativeLootChest(@NotNull RegenerativeLootChest regenerativeLootChest);


    /**
     * Checks if the given loot chest template identifier exists
     */
    boolean isLootChestTemplate(@NotNull String identifier);

    /**
     * Gets all possible chest templates.
     */
    @NotNull
    Collection<LootChestTemplate> getChestTemplates();

    /**
     * Gets all possible regenerative loot chests.
     */
    @NotNull
    Collection<RegenerativeLootChest> getRegenerativeLootChests();

    /**
     * Begins displaying a timed loot chest
     */
    void displayTimedLootChest(@NotNull Player player, @NotNull TimedLootChest chest);

    /**
     * Gets the boss timed loot with a given identifier, or null if none exists
     */
    @Nullable
    BossTimedLoot getBossTimedLoot(@NotNull String mmID);

    /**
     * Gets the custom timed loot with a given identifier, or null if none exists
     */
    @Nullable
    CustomTimedLoot getCustomTimedLoot(@NotNull String identifier);

}