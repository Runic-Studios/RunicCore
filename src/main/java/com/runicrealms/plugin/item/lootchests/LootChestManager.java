package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class LootChestManager {

    private RunicCore plugin = RunicCore.getInstance();

    public LootChestManager() {

        new BukkitRunnable() {
            @Override
            public void run() {
                particleTask();
            }
        }.runTaskTimer(this.plugin, 20, 3*20L);
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
