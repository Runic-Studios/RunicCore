package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class LootChestManager {

    private RunicCore plugin = RunicCore.getInstance();

    public LootChestManager() {

        new BukkitRunnable() {
            @Override
            public void run() {
                regenChests();
            }
        }.runTaskTimer(this.plugin, 100, 60L);

        new BukkitRunnable() {
            @Override
            public void run() {
                particleTask();
            }
        }.runTaskTimer(this.plugin, 20, 3*20L);
    }

    private void regenChests() {

        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestLocations = YamlConfiguration.loadConfiguration(chests);
        ConfigurationSection locations = chestLocations.getConfigurationSection("Chests.Locations");

        for (String id : locations.getKeys(false)) {
            World world = Bukkit.getWorld(Objects.requireNonNull(locations.getString(id + ".world")));
            double x = locations.getDouble(id + ".x");
            double y = locations.getDouble(id + ".y");
            double z = locations.getDouble(id + ".z");
            Location loc = new Location(world, x, y, z);
            if (loc.getBlock().getType() == Material.CHEST) continue;
            loc.getBlock().setType(Material.CHEST);
            loc.getBlock().getWorld().spawnParticle(Particle.VILLAGER_HAPPY,
                    loc.getBlock().getLocation().add(0.5, 0, 0.5), 25, 0.5, 0.5, 0.5, 0.01);
        }
    }

    private void particleTask() {

        // retrieve the data file
        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);
        ConfigurationSection chestLocs = chestConfig.getConfigurationSection("Chests.Locations");

        if (chestLocs == null) return;

        for (String id : chestLocs.getKeys(false)) {
            String tier = chestLocs.getString(id + ".tier");
            String world = chestLocs.getString(id + ".world");
            double x = chestLocs.getDouble(id + ".x");
            double y = chestLocs.getDouble(id + ".y");
            double z = chestLocs.getDouble(id + ".z");
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);

            if (loc.getBlock().getType() != Material.CHEST) continue;

            Color color;
            switch (tier) {
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

            Bukkit.getWorld(world).spawnParticle(Particle.REDSTONE, loc.clone().add(0.5, 0.5, 0.5),
                    10, 0.25f, 0.25f, 0.25f, 0, new Particle.DustOptions(color, 3));
        }
    }
}
