package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

// todo: make lootchest its own object, map a lootchest to the current system time to have per-chest respawns.
@SuppressWarnings("FieldCanBeLocal")
public class LootChestManager {

    private RunicCore plugin = RunicCore.getInstance();
    private static final int RESPAWN_TIME = 15; // minutes
    private HashMap<Location, String> chestLocs = new HashMap<>();

    public LootChestManager() {

        // store all chest locations in a hashmap
        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestLocations = YamlConfiguration.loadConfiguration(chests);
        ConfigurationSection locations = chestLocations.getConfigurationSection("Chests.Locations");

        if (locations == null) return;

        for (String id : locations.getKeys(false)) {
            String tier = locations.getString(id + ".tier");
            World world = Bukkit.getWorld(Objects.requireNonNull(locations.getString(id + ".world")));
            double x = locations.getDouble(id + ".x");
            double y = locations.getDouble(id + ".y");
            double z = locations.getDouble(id + ".z");
            Location loc = new Location(world, x, y, z);
            chestLocs.put(loc, tier);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                regenChests();
            }
        }.runTaskTimer(this.plugin, 100, RESPAWN_TIME*60*20L); // time * seconds / ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                particleTask();
            }
        }.runTaskTimer(this.plugin, 20, 3*20L);
    }

    /**
     * This method generates green particles when a chest respawns and sets the material to a chest.
     */
    private void regenChests() {
        for (Location loc : chestLocs.keySet()) {
            if (loc.getBlock().getType() == Material.CHEST) continue;
            loc.getBlock().setType(Material.CHEST);
            loc.getBlock().getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    loc.getBlock().getLocation().add(0.5, 0, 0.5), 25, 0.5, 0.5, 0.5, 0.01);
        }
    }

    private void particleTask() {

        for (Location loc : chestLocs.keySet()) {
            if (loc.getBlock().getType() != Material.CHEST) continue;
            Color color;
            switch (chestLocs.get(loc)) {
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
}
