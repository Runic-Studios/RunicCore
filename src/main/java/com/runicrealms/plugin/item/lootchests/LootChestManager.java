package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LootChestManager {

    private final HashSet<LootChest> lootChests;
    private final ConcurrentHashMap<UUID, Map<LootChest, Long>> playerChestCooldownMap; // maps player to which chests they have looted
    private final File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
            "chests.yml");
    private final FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);
    private final ConfigurationSection locations = chestConfig.getConfigurationSection("Chests.Locations");

    public LootChestManager() {

        lootChests = new HashSet<>();
        playerChestCooldownMap = new ConcurrentHashMap<>();
        if (locations == null) return;

        /*
        Initial spawning of chests from flat file storage
         */
        try {
            for (String id : locations.getKeys(false)) {
                String tier = locations.getString(id + ".tier");
                LootChestTier lootChestTier = LootChestTier.getFromIdentifier(tier);
                if (lootChestTier == null) continue;
                String worldName = locations.getString(id + ".world");
                World world = Bukkit.getWorld(worldName);
                double x = locations.getDouble(id + ".x");
                double y = locations.getDouble(id + ".y");
                double z = locations.getDouble(id + ".z");
                Location loc = new Location(world, x, y, z);
                LootChest lootChest = new LootChest(id, lootChestTier, loc);
                lootChests.add(lootChest);
                lootChest.getLocation().getBlock().setType(Material.CHEST);
            }
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "Error: there was an error loading loot chests!");
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), this::regenChests, 100L, 3 * 20L); // time * seconds / ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), this::particleTask, 20L, 3 * 20L);
    }

    /**
     * Returns a map of the chests which are on cooldown for the given uuid, along with their remaining CD time
     *
     * @param uuid of the player to check
     * @return a set of chests on cooldown
     */
    public Map<LootChest, Long> getChestsOnCDForUuid(UUID uuid) {
        playerChestCooldownMap.putIfAbsent(uuid, new ConcurrentHashMap<>());
        return playerChestCooldownMap.get(uuid);
    }

    /**
     * Grabs the LootChest object associated w/ the given location
     *
     * @param location of chest
     * @return a LootChest object
     */
    public LootChest getLootChest(Location location) {
        for (LootChest lootChest : lootChests) {
            if (lootChest.getLocation().equals(location))
                return lootChest;
        }
        return null;
    }

    /**
     * Grabs our in-memory set of loot chests
     *
     * @return a HashSet of loot chests
     */
    public HashSet<LootChest> getLootChests() {
        return lootChests;
    }

    /**
     * Get a map containing the player's chest cooldowns
     *
     * @return a Map of uuid to a set of chests on cooldown
     */
    public Map<UUID, Map<LootChest, Long>> getPlayerChestCooldownMap() {
        return playerChestCooldownMap;
    }

    /**
     * Checks whether there is a loot chest at target location.
     *
     * @param location of loot chest
     * @return true if found, false if not
     */
    public boolean isLootChest(Location location) {
        return RunicCore.getLootChestManager().getLootChest(location) != null; // chest doesn't match saved chest locations
    }

    /**
     * Generates particles based on the loot chest color for all players who have not looted the chest
     */
    private void particleTask() {
        for (LootChest lootChest : lootChests) {
            if (!lootChest.getLocation().isWorldLoaded()) continue;
            World world = lootChest.getLocation().getWorld();
            assert world != null;
            if (!world.isChunkLoaded(lootChest.getLocation().getChunk())) continue;
            Location location = lootChest.getLocation();
            if (location.getBlock().getType() != Material.CHEST) continue;
            for (UUID loaded : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                Player online = Bukkit.getPlayer(loaded);
                if (online == null) continue; // Player offline
                if (playerChestCooldownMap.containsKey(loaded)) {
                    Map<LootChest, Long> chestsOnCooldown = playerChestCooldownMap.get(online.getUniqueId());
                    if (chestsOnCooldown.containsKey(lootChest))
                        continue; // only display particles to players who are not on cooldown
                }
                if (!online.getWorld().equals(world))
                    continue; // They must be in the same world as this chest
                online.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 0.5, 0.5),
                        10, 0.25f, 0.25f, 0.25f, 0, new Particle.DustOptions(lootChest.getLootChestRarity().getColor(), 3));
            }
        }
    }

    /**
     * Respawns chest based upon their respawn time left
     */
    private void regenChests() {
        for (UUID uuid : playerChestCooldownMap.keySet()) {
            if (playerChestCooldownMap.get(uuid) == null) continue;
            for (LootChest lootChest : playerChestCooldownMap.get(uuid).keySet()) {
                if ((System.currentTimeMillis() - playerChestCooldownMap.get(uuid).get(lootChest))
                        < lootChest.getLootChestRarity().getRespawnTimeSeconds() * 1000L)
                    continue; // chest not finished CD
                playerChestCooldownMap.get(uuid).remove(lootChest);
            }
        }
    }

    /**
     * Completely erases a loot chest at target location,
     * both removing it from memory and config.
     *
     * @param location location of chest
     */
    public void removeLootChest(Location location) {
        LootChest chestToRemove = getLootChest(location);
        lootChests.remove(chestToRemove); // remove chest from memory
        try {
            for (String chest_id : locations.getKeys(false)) {
                if (!chest_id.equals(chestToRemove.getId())) continue;
                locations.set(chest_id, null);
            }
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "Error: there was an error removing loot chest!");
            e.printStackTrace();
        }
        try {
            chestConfig.save(chests);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
