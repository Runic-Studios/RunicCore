package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class provides useful methods for data reading and writing
 */
public class DatabaseHelper {

    public static final String SIMPLE_DATE_STRING;
    public static final Bson LAST_LOGIN_DATE_FILTER;

    static {
        LocalDate localDate = LocalDate.now();
        LocalDate oneMonthAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        SIMPLE_DATE_STRING = localDate.format(formatter);
        LAST_LOGIN_DATE_FILTER = Filters.gte("last_login", oneMonthAgo);
    }

    /**
     * Serializes a character's location data so that it may be stored as a readable string in redis
     *
     * @param location the location of a currently selected character
     * @return a string for storage in redis
     */
    public static String serializeLocation(Location location) {
        World world = location.getWorld();
        assert world != null;
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        return world.getName() + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }

    /**
     * Deserializes a character's location data from a redis string
     *
     * @param serializedLocation the serialized string
     * @return a Location object
     */
    public static Location loadLocationFromSerializedString(String serializedLocation) {
        try {
            String[] values = serializedLocation.split(":");
            World world = Bukkit.getWorld(values[0]);
            double x = Double.parseDouble(values[1]);
            double y = Double.parseDouble(values[2]);
            double z = Double.parseDouble(values[3]);
            float yaw = Float.parseFloat(values[4]);
            float pitch = Float.parseFloat(values[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().info("Error: legacy player location detected, re-spawning in tutorial!");
            return CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK); // return hearth location
        }
    }

//    /**
//     * This method sets the player's location in the given data section of their mongo document
//     *
//     * @param mongoDataSection the character section of player data
//     * @param location         the location of the player cache (or hearthstone location)
//     */
//    public static void saveLocation(PlayerMongoDataSection mongoDataSection, Location location) {
//        try {
//            mongoDataSection.set("loc.world", location.getWorld().getName());
//            mongoDataSection.set("loc.x", location.getX());
//            mongoDataSection.set("loc.y", location.getY());
//            mongoDataSection.set("loc.z", location.getZ());
//            mongoDataSection.set("loc.yaw", (int) location.getYaw());
//            mongoDataSection.set("loc.pitch", (int) location.getPitch());
//        } catch (Exception e) {
//            Bukkit.getLogger().info(ChatColor.RED + "Save location method encountered an exception!");
//            e.printStackTrace();
//        }
//    }

}
