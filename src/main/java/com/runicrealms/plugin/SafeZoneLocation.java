package com.runicrealms.plugin;

import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public enum SafeZoneLocation {

    TUTORIAL("tutorial", "Tutorial",
            new Location(Bukkit.getWorld("Alterra"), -2277.5, 23, 1676.5, -250, 0), HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK),
    AZANA("azana", "Azana",
            new Location(Bukkit.getWorld("Alterra"), -764.5, 40, 206.5, 180, 0), HearthstoneItemUtil.HEARTHSTONE_AZANA_ITEMSTACK),
    KOLDORE("koldore", "Koldore",
            new Location(Bukkit.getWorld("Alterra"), -1661.5, 35, 206.5, 270, 0), HearthstoneItemUtil.HEARTHSTONE_KOLDORE_ITEMSTACK),
    WHALETOWN("whaletown", "Whaletown",
            new Location(Bukkit.getWorld("Alterra"), -1834.5, 32, -654.5), HearthstoneItemUtil.HEARTHSTONE_WHALETOWN_ITEMSTACK),
    HILSTEAD("hilstead", "Hilstead",
            new Location(Bukkit.getWorld("Alterra"), -1649.5, 44, -2053.5, 270, 0), HearthstoneItemUtil.HEARTHSTONE_HILSTEAD_ITEMSTACK),
    WINTERVALE("wintervale", "Wintervale",
            new Location(Bukkit.getWorld("Alterra"), -1672.5, 37, -2639.5, 90, 0), HearthstoneItemUtil.HEARTHSTONE_WINTERVALE_ITEMSTACK),
    DAWNSHIRE_INN("dawnshire_inn", "Dawnshire Inn",
            new Location(Bukkit.getWorld("Alterra"), -306.5, 57, -408.5, 90, 0), HearthstoneItemUtil.HEARTHSTONE_DAWNSHIRE_INN_ITEMSTACK),
    DEAD_MANS_REST("dead_mans_rest", "Dead Man's Rest",
            new Location(Bukkit.getWorld("Alterra"), -24.5, 32, -475.5, 90, 0), HearthstoneItemUtil.HEARTHSTONE_DEAD_MANS_REST_ITEMSTACK),
    ISFODAR("isfodar", "Isfodar",
            new Location(Bukkit.getWorld("Alterra"), 754.5, 113, -93.5, 0, 0), HearthstoneItemUtil.HEARTHSTONE_ISFODAR_ITEMSTACK),
    TIRENEAS("tireneas", "Tireneas",
            new Location(Bukkit.getWorld("Alterra"), 887.5, 43, 547.5, 270, 0), HearthstoneItemUtil.HEARTHSTONE_TIRENEAS_ITEMSTACK),
    ZENYTH("zenyth", "Zenyth",
            new Location(Bukkit.getWorld("Alterra"), 1564.5, 38, -158.5, 180, 0), HearthstoneItemUtil.HEARTHSTONE_ZENYTH_ITEMSTACK),
    NAHEEN("naheen", "Naheen",
            new Location(Bukkit.getWorld("Alterra"), 1962.5, 42, 349.5, 270, 0), HearthstoneItemUtil.HEARTHSTONE_NAHEEN_ITEMSTACK),
    NAZMORA("nazmora", "Naz'mora",
            new Location(Bukkit.getWorld("Alterra"), 2587.5, 33, 979.5, 270, 0), HearthstoneItemUtil.HEARTHSTONE_NAZMORA_ITEMSTACK),
    ORC_OUTPOST("orc_outpost", "Orc Outpost",
            new Location(Bukkit.getWorld("Alterra"), 2587.5, 33, 979.5, 270, 0), null, false),
    STONEHAVEN("stonehaven", "Stonehaven",
            new Location(Bukkit.getWorld("Alterra"), -788.5, 37, 749.5, 90, 0), HearthstoneItemUtil.HEARTHSTONE_STONEHAVEN_ITEMSTACK),
    FROSTS_END("frosts_end", "Frost's End",
            new Location(Bukkit.getWorld("Alterra"), -788.5, 37, 749.5, 90, 0), null, false);

    private final String identifier;
    private final String display;
    private final Location location;
    private final ItemStack itemStack;
    private final boolean isCity; // True if it should be included in the fast travel

    SafeZoneLocation(String identifier, String display, Location location, ItemStack itemStack) {
        this.identifier = identifier;
        this.display = display;
        this.location = location;
        this.itemStack = itemStack;
        this.isCity = true;
    }

    SafeZoneLocation(String identifier, String display, Location location, ItemStack itemStack, boolean isCity) {
        this.identifier = identifier;
        this.display = display;
        this.location = location;
        this.itemStack = itemStack;
        this.isCity = isCity;
    }

    /**
     * Returns an enum based on a string identifier
     *
     * @param identifier the location of the hearthstone
     * @return an enum
     */
    public static SafeZoneLocation getFromIdentifier(String identifier) {
        for (SafeZoneLocation safeZoneLocation : SafeZoneLocation.values()) {
            if (safeZoneLocation.getIdentifier().equals(identifier))
                return safeZoneLocation;
        }
        return SafeZoneLocation.TUTORIAL;
    }

    /**
     * Returns the location of hearthstone based on its string identifier
     *
     * @param identifier the value that goes under 'location' in the yaml
     * @return a Location object
     */
    public static Location getLocationFromIdentifier(String identifier) {
        for (SafeZoneLocation safeZoneLocation : SafeZoneLocation.values()) {
            if (!safeZoneLocation.isCity) continue; // Ignore quest hubs which don't have hearthstones
            if (safeZoneLocation.identifier.equals(identifier))
                return safeZoneLocation.getLocation();
        }
        return SafeZoneLocation.TUTORIAL.getLocation();
    }

    /**
     * Returns the location of a hearthstone based on the identifier of the item stack
     *
     * @param hearthstone is the player's hearthstone
     * @return a Location object
     */
    public static Location getLocationFromItemStack(ItemStack hearthstone) {
        RunicItem runicItemHearthstone = RunicItemsAPI.getRunicItemFromItemStack(hearthstone);
        String identifier = runicItemHearthstone.getData().get("location");
        return getLocationFromIdentifier(identifier);
    }

    public boolean isCity() {
        return isCity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplay() {
        return display;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
