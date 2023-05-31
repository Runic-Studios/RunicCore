package com.runicrealms.plugin.config;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.exception.ShopLoadException;
import com.runicrealms.plugin.item.shops.RunicShopGeneric;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads shop files from yml during startup
 */
public class ShopConfigLoader {

    /*
    Static block to load our shop list into memory from file storage on startup
     */
    static {
        File shopsFolder = RunicCommon.getConfigAPI().getSubFolder(RunicCore.getInstance().getDataFolder(), "shops");
        for (File shopFile : shopsFolder.listFiles()) {
            if (shopFile.isDirectory()) continue; // ignore subdirectories
            try {
                // noinspection unused
                RunicShopGeneric ignored = loadShop(RunicCommon.getConfigAPI().getYamlConfigFromFile(shopFile.getName(), shopsFolder)); // adds to in-memory cache here
            } catch (ShopLoadException exception) {
                exception.addMessage("Error loading shop for file: " + shopFile.getName());
                exception.displayToConsole();
                exception.displayToOnlinePlayers();
            }
        }
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
            ArrayList<RunicShopItem> shopItems = new ArrayList<>();

            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            for (String itemString : itemsSection.getKeys(false)) {
                RunicShopItem runicShopItem;
                try {
                    String runicItemId = itemsSection.getString(itemString + ".id");
                    runicShopItem = loadShopItem(itemsSection.getConfigurationSection(itemString), runicItemId);
                } catch (ShopLoadException exception) {
                    exception.addMessage(itemString + "", "item: " + itemString);
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
            int stacksize = 1;
            List<Pair<String, Integer>> requiredItems = new ArrayList<>();
            for (String key : section.getKeys(false)) {
                if (key.equalsIgnoreCase("stack-size")) {
                    stacksize = section.getInt("stack-size");
                } else if (!key.equalsIgnoreCase("id")) {
                    requiredItems.add(Pair.pair(key, section.getInt(key)));
                }
            }
            ItemStack item = RunicItemsAPI.generateItemFromTemplate(runicItemId).generateGUIItem();
            item.setAmount(stacksize);
            return new RunicShopItem(requiredItems, item);
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
