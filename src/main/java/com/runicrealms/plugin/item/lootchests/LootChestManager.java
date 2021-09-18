package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

public class LootChestManager {

    private static final int TASK_INTERVAL = 15; // seconds

    private final HashSet<LootChest> lootChests; // maps chest to respawn time
    private final LinkedHashMap<LootChest, Long> queuedChests;

    private final File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
            "chests.yml");
    private final FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);
    private final ConfigurationSection locations = chestConfig.getConfigurationSection("Chests.Locations");

    public LootChestManager() {

        lootChests = new HashSet<>();
        queuedChests = new LinkedHashMap<>();
        if (locations == null) return;

        /*
        Initial spawning of chests from flat file storage
         */
        try {
            for (String id : locations.getKeys(false)) {
                String tier = locations.getString(id + ".tier");
                LootChestRarity lootChestRarity = LootChestRarity.getFromIdentifier(tier);
                if (lootChestRarity == null) continue;
                World world = Bukkit.getWorld(Objects.requireNonNull(locations.getString(id + ".world")));
                double x = locations.getDouble(id + ".x");
                double y = locations.getDouble(id + ".y");
                double z = locations.getDouble(id + ".z");
                Location loc = new Location(world, x, y, z);
                LootChest lootChest = new LootChest(id, lootChestRarity, loc);
                lootChests.add(lootChest);
                lootChest.getLocation().getBlock().setType(Material.CHEST);
            }
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "Error: there was an error loading loot chests!");
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), this::regenChests, 100L, TASK_INTERVAL * 20L); // time * seconds / ticks
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), this::particleTask, 20L, 3 * 20L);
    }

    /**
     * Respawns chest based upon their respawn time left
     */
    private void regenChests() {

        int limit = (int) Math.ceil(queuedChests.size() / 4) + 1;
        int count = 0;

        Set<LootChest> remove = new HashSet<>();
        for (LootChest chest : queuedChests.keySet()) {
            if (queuedChests.size() < 1) continue;
            if (count >= limit) break;
            Location loc = chest.getLocation();
            if (!Objects.requireNonNull(loc.getWorld()).isChunkLoaded(loc.getChunk())) continue; // chunk must be loaded
            if (loc.getBlock().getType() == Material.CHEST) continue; // chest already loaded
            if ((System.currentTimeMillis() - queuedChests.get(chest)) < matchTime(chest) * 1000L) continue;
            loc.getBlock().setType(Material.CHEST);
            remove.add(chest);
            count++;
        }
        for (LootChest chest : remove) {
            queuedChests.remove(chest);
        }
    }

    /**
     * Returns an int, seconds, for how often a chest should spawn, based on tier.
     */
    private int matchTime(LootChest chest) {
        switch (chest.getLootChestRarity()) {
            case UNCOMMON:
                return 900; // 15 min
            case RARE:
                return 1200; // 20 min
            case EPIC:
                return 2700; // 45 min
            default:
                return 600; // 10 min for common
        }
    }

    private void particleTask() {

        for (LootChest lootChest : lootChests) {
            if (!Objects.requireNonNull(lootChest.getLocation().getWorld()).isChunkLoaded(lootChest.getLocation().getChunk()))
                continue;
            Location loc = lootChest.getLocation();
            if (loc.getBlock().getType() != Material.CHEST) continue;
            Color color;
            switch (lootChest.getLootChestRarity()) {
                case COMMON:
                    color = Color.WHITE;
                    break;
                case UNCOMMON:
                    color = Color.LIME;
                    break;
                case RARE:
                    color = Color.AQUA;
                    break;
                default:
                    color = Color.FUCHSIA;
                    break;
            }

            Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.REDSTONE, loc.clone().add(0.5, 0.5, 0.5),
                    10, 0.25f, 0.25f, 0.25f, 0, new Particle.DustOptions(color, 3));
        }
    }

    public LootChest getLootChest(Location loc) {
        for (LootChest lootChest : lootChests) {
            if (lootChest.getLocation().equals(loc))
                return lootChest;
        }
        return null;
    }

    public HashSet<LootChest> getLootChests() {
        return lootChests;
    }

    public LinkedHashMap<LootChest, Long> getQueuedChests() {
        return queuedChests;
    }

    public LootChest getQueuedChest(Location loc) {
        for (LootChest queuedChest : queuedChests.keySet()) {
            if (queuedChest.getLocation().equals(loc))
                return queuedChest;
        }
        return null;
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
