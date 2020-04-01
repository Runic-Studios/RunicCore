package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Objects;

// todo: make lootchest its own object, map a lootchest to the current system time to have per-chest respawns.
@SuppressWarnings("FieldCanBeLocal")
public class LootChestManager {

    private RunicCore plugin = RunicCore.getInstance();
    private static final long RESPAWN_TIME = 15; // minutes
    private static final int TASK_INTERVAL = 10;
    private HashMap<LootChest, Long> lootChests = new HashMap<>(); // maps chest to respawn time

    public LootChestManager() {

//        // store all chest locations in a hashmap
//        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
//                "chests.yml");
//        FileConfiguration chestLocations = YamlConfiguration.loadConfiguration(chests);
//        ConfigurationSection locations = chestLocations.getConfigurationSection("Chests.Locations");
//
//        if (locations == null) return;
//
//        for (String id : locations.getKeys(false)) {
//            String tier = locations.getString(id + ".tier");
//            World world = Bukkit.getWorld(Objects.requireNonNull(locations.getString(id + ".world")));
//            double x = locations.getDouble(id + ".x");
//            double y = locations.getDouble(id + ".y");
//            double z = locations.getDouble(id + ".z");
//            Location loc = new Location(world, x, y, z);
//            LootChest lootChest = new LootChest(tier, loc);
//            lootChests.put(lootChest, RESPAWN_TIME);
//        }
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                regenChests();
//            }
//        }.runTaskTimer(this.plugin, 100, TASK_INTERVAL*20L); // time * seconds / ticks
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                particleTask();
//            }
//        }.runTaskTimer(this.plugin, 20, 3*20L);
    }

    /**
     * This method generates green particles when a chest respawns and sets the material to a chest.
     */
    private void regenChests() {
        for (LootChest lootChest : lootChests.keySet()) {
            if (!Objects.requireNonNull(lootChest.getLocation().getWorld()).isChunkLoaded(lootChest.getLocation().getChunk())) continue; // chunk must be loaded
            if (lootChest.getLocation().getBlock().getType() == Material.CHEST) continue; // chest already loaded
            if ((System.currentTimeMillis()-lootChests.get(lootChest)) < RESPAWN_TIME*1000) continue; // TODO: ADD TIMES 60 FOR MINUTES
            lootChest.getLocation().getBlock().setType(Material.CHEST);
            lootChest.getLocation().getBlock().getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    lootChest.getLocation().getBlock().getLocation().add(0.5, 0, 0.5), 25, 0.5, 0.5, 0.5, 0.01);
        }
    }

    private void particleTask() {

        for (LootChest lootChest : lootChests.keySet()) {
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

    public HashMap<LootChest, Long> getLootChests() {
        return lootChests;
    }

    public LootChest getLootChest(Location loc) {
        for (LootChest lootChest : lootChests.keySet()) {
            if (lootChest.getLocation().equals(loc))
                return lootChest;
        }
        return null;
    }
}
