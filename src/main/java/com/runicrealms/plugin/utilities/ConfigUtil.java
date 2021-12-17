package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigUtil {

    private static final File DUNGEONS_FILE = new File(RunicCore.getInstance().getDataFolder(), "dungeons.yml");
    private static final FileConfiguration DUNGEONS_FILE_CONFIG = YamlConfiguration.loadConfiguration(DUNGEONS_FILE);

    public static File getDungeonsFile() {
        return DUNGEONS_FILE;
    }

    public static FileConfiguration getDungeonsFileConfig() {
        return DUNGEONS_FILE_CONFIG;
    }

    public static ConfigurationSection getDungeonConfigurationSection() {
        return DUNGEONS_FILE_CONFIG.getConfigurationSection("dungeons");
    }

}
