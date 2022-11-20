package com.runicrealms.plugin.api;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public interface ConfigAPI {

    /**
     * Gets a subdirectory from within a plugin (i.e., RunicQuests/quests)
     *
     * @param folder    the parent folder of the intended subdirectory
     * @param subfolder the string name of the subdirectory
     * @return the subdirectory if found, else null
     */
    File getSubFolder(File folder, String subfolder);

    /**
     * Loads a YamlConfiguration from a File object
     *
     * @param fileName name of the file
     * @param folder   the subfolder in the plugin directory
     * @return a FileConfiguration object
     */
    FileConfiguration getYamlConfigFile(String fileName, File folder);
}
