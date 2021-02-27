package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("FieldCanBeLocal")
public class LootChestManager {

    private static final int TASK_INTERVAL = 15; // seconds

    private final HashSet<LootChest> lootChests; // maps chest to respawn time
    private final LinkedHashMap<LootChest, Long> queuedChests;
    private final RunicCore plugin = RunicCore.getInstance();

    public LootChestManager() {

        lootChests = new HashSet<>();
        queuedChests = new LinkedHashMap<>();

        // store all chest locations in a set
        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestLocations = YamlConfiguration.loadConfiguration(chests);
        ConfigurationSection locations = chestLocations.getConfigurationSection("Chests.Locations");

        if (locations == null) return;

        /*
        Initial spawning of chests from flatfile storage
         */
        try {
            for (String id : locations.getKeys(false)) {
                String tier = locations.getString(id + ".tier");
                World world = Bukkit.getWorld(Objects.requireNonNull(locations.getString(id + ".world")));
                double x = locations.getDouble(id + ".x");
                double y = locations.getDouble(id + ".y");
                double z = locations.getDouble(id + ".z");
                Location loc = new Location(world, x, y, z);
                LootChest lootChest = new LootChest(tier, loc);
                lootChests.add(lootChest);
                lootChest.getLocation().getBlock().setType(Material.CHEST);
            }
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "Error: there was an error loading loot chests!");
            e.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                regenChests();
            }
        }.runTaskTimer(this.plugin, 100, TASK_INTERVAL*20L); // time * seconds / ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                particleTask();
            }
        }.runTaskTimerAsynchronously(this.plugin, 20, 3*20L);
    }

    /**
     * Respawns chest based upon their respawn time left
     */
    private void regenChests() {

        int limit = (int) Math.ceil(queuedChests.size() / 4) + 1;
        int count = 0;

        Set<LootChest> remove = new HashSet<LootChest>();
        for (LootChest chest : queuedChests.keySet()) {
            if (queuedChests.size() < 1) continue;
            if (count >= limit) break;
            Location loc = chest.getLocation();
            if (!Objects.requireNonNull(loc.getWorld()).isChunkLoaded(loc.getChunk())) continue; // chunk must be loaded
            if (loc.getBlock().getType() == Material.CHEST) continue; // chest already loaded
            if ((System.currentTimeMillis()-queuedChests.get(chest)) < matchTime(chest)*1000) continue;
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
        switch (chest.getTier()) {
            case "uncommon":
                return 900; // 15 min
            case "rare":
                return 1200; // 20 min
            case "epic":
                return 2700; // 45 min
            default:
                return 600; // 10 min for common
        }
    }

    private void particleTask() {

        for (LootChest lootChest : lootChests) {
            if (!Objects.requireNonNull(lootChest.getLocation().getWorld()).isChunkLoaded(lootChest.getLocation().getChunk())) continue;
            Location loc = lootChest.getLocation();
            if (loc.getBlock().getType() != Material.CHEST) continue;
            Color color;
            switch (lootChest.getTier()) {
                case "common":
                    color = Color.WHITE;
                    break;
                case "uncommon":
                    color = Color.LIME;
                    break;
                case "rare":
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
}
