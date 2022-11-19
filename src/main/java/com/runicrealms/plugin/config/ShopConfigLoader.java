package com.runicrealms.plugin.config;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.exception.ShopLoadException;
import com.runicrealms.plugin.item.shops.RunicShopGeneric;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * Loads shop files from yml during startup
 */
public class ShopConfigLoader {

    /*
    Static block to load our shop list into memory from file storage on startup
     */
    static {
        File shopsFolder = getSubFolder(RunicCore.getInstance().getDataFolder(), "shops");
        for (File shopFile : shopsFolder.listFiles()) {
            if (shopFile.isDirectory()) continue; // ignore subdirectories
            try {
                // noinspection unused
                RunicShopGeneric ignored = loadShop(getYamlConfigFile(shopFile.getName(), shopsFolder)); // adds to in-memory cache here
            } catch (ShopLoadException exception) {
                exception.addMessage("Error loading shop for file: " + shopFile.getName());
                exception.displayToConsole();
                exception.displayToOnlinePlayers();
            }
        }
    }

    /**
     * Loads a YamlConfiguration from a File object
     *
     * @param fileName name of the file
     * @param folder   the subfolder in the plugin directory
     * @return a FileConfiguration object
     */
    public static FileConfiguration getYamlConfigFile(String fileName, File folder) {
        FileConfiguration config;
        File file;
        file = new File(folder, fileName);
        config = new YamlConfiguration();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            config.load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return config;
    }

    /**
     * Gets a subdirectory
     *
     * @param folder    the parent folder of the intended subdirectory
     * @param subfolder the string name of the subdirectory
     * @return the subdirectory if found, else null
     */
    public static File getSubFolder(File folder, String subfolder) {
        assert folder != null; // main folder
        for (File file : folder.listFiles()) {
            if (file.getName().equalsIgnoreCase(subfolder)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Loads a shop from its corresponding yml file
     *
     * @param config the file configuration of the shop file
     * @return a RunicShopGeneric
     * @throws ShopLoadException if the syntax is configured incorrectly
     */
    public static RunicShopGeneric loadShop(FileConfiguration config) throws ShopLoadException {
        try {
            String name = config.getString("name");
            int size = config.getInt("size");
            LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();

            for (String itemId : config.getConfigurationSection("items").getKeys(false)) {
                RunicShopItem runicShopItem;
                try {
                    runicShopItem = loadShopItem(config.getConfigurationSection("items." + itemId), itemId);
                } catch (ShopLoadException exception) {
                    exception.addMessage(itemId + "", "item: " + itemId);
                    throw exception;
                }
                shopItems.add(runicShopItem);
            }

            Set<Integer> npcs = new HashSet<>();
            for (String string : config.getStringList("npcs")) {
                npcs.add(Integer.parseInt(string));
            }

            if (!config.getStringList("inventorySlots").isEmpty()) {
                List<Integer> slotsList = new ArrayList<>();
                for (String slot : config.getStringList("inventorySlots")) {
                    slotsList.add(Integer.parseInt(slot));
                }
                int[] inventorySlots = new int[slotsList.size()];
                int index = 0;
                for (final Integer value : slotsList) {
                    inventorySlots[index++] = value;
                }

                return new RunicShopGeneric(size, ChatColor.YELLOW + name, npcs, shopItems, inventorySlots);
            } else {
                return new RunicShopGeneric(size, ChatColor.YELLOW + name, npcs, shopItems);
            }

        } catch (ShopLoadException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ShopLoadException("unknown syntax error").setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Loads a RunicShopItem from its section of a shop config
     *
     * @param section     of the item
     * @param runicItemId of the item
     * @return a RunicShopItem
     * @throws ShopLoadException if incorrectly configured
     */
    public static RunicShopItem loadShopItem(ConfigurationSection section, String runicItemId) throws ShopLoadException {
        try {
            int price = section.getInt("price");
            String currencyId = section.getString("currency");
            Bukkit.getLogger().warning(price + " is price");
            Bukkit.getLogger().warning(currencyId + " is id of currency");
            Bukkit.getLogger().warning(runicItemId + " is runic item id");
            return new RunicShopItem(price, currencyId, RunicItemsAPI.generateItemFromTemplate(runicItemId).generateGUIItem());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ShopLoadException("item initialization syntax error for " + runicItemId).setErrorMessage(exception.getMessage());
        }
    }

    /**
     * Dummy method to force the class to load
     */
    public static void init() {

    }
}
