package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
     * Attempts to populate the document for a new character slot with default values
     *
     * @param playerMongoData of the player who created a new character
     * @param className       the name of the class
     * @param slot            the slot of the character
     */
    public static void addNewCharacter(PlayerMongoData playerMongoData, String className, Integer slot, final WriteCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            MongoDataSection mongoDataSection = playerMongoData.getSection("character." + slot);
            mongoDataSection.set("class.name", className);
            mongoDataSection.set("class.level", 0);
            mongoDataSection.set("class.exp", 0);
            mongoDataSection.set("prof.name", "None");
            mongoDataSection.set("prof.level", 0);
            mongoDataSection.set("prof.exp", 0);
            mongoDataSection.set("currentHp", HealthUtils.getBaseHealth());
            mongoDataSection.set("maxMana", RegenManager.getBaseMana());
            mongoDataSection.set("storedHunger", 20);
            mongoDataSection.set("outlaw.enabled", false);
            mongoDataSection.set("outlaw.rating", RunicCore.getBaseOutlawRating());
            DatabaseHelper.saveLocation(playerMongoData.getCharacter(slot), CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK)); // tutorial
            playerMongoData.save();
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), callback::onWriteComplete);
        });
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

    /**
     * Deserializes a character's location data from a redis string
     *
     * @param mongoDataSection of the player
     * @return a Location object
     */
    public static Location loadLocationFromSerializedString(PlayerMongoDataSection mongoDataSection) {
        try {
            World world = Bukkit.getWorld(mongoDataSection.get(("loc.world"), String.class));
            double x = mongoDataSection.get("loc.x", double.class);
            double y = mongoDataSection.get("loc.y", double.class);
            double z = mongoDataSection.get("loc.z", double.class);
            float yaw = Float.parseFloat(String.valueOf(mongoDataSection.get("loc.yaw", int.class)));
            float pitch = Float.parseFloat(String.valueOf(mongoDataSection.get("loc.pitch", int.class)));
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            // return hearth location
            Bukkit.getLogger().info("Error: legacy player location detected, re-spawning in tutorial!");
            return CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK);
        }
    }

    /**
     * This method sets the player's location in the given data section of their mongo document
     *
     * @param mongoDataSection the character section of player data
     * @param location         the location of the player cache (or hearthstone location)
     */
    public static void saveLocation(PlayerMongoDataSection mongoDataSection, Location location) {
        try {
            mongoDataSection.set("loc.world", location.getWorld().getName());
            mongoDataSection.set("loc.x", location.getX());
            mongoDataSection.set("loc.y", location.getY());
            mongoDataSection.set("loc.z", location.getZ());
            mongoDataSection.set("loc.yaw", (int) location.getYaw());
            mongoDataSection.set("loc.pitch", (int) location.getPitch());
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Save location method encountered an exception!");
            e.printStackTrace();
        }
    }

    /**
     * Creates a CharacterData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    public static void loadCharacterData(UUID uuid, Integer slot, Jedis jedis, final ReadCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            // Step 1: check if character data is cached in redis
            CharacterData characterDataRedis = RunicCore.getRedisManager().checkRedisForCharacterData(uuid, slot, jedis);
            if (characterDataRedis != null) {
                Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> callback.onQueryComplete(characterDataRedis));
            }
            // Step 2: check mongo documents
            else {
                CharacterData characterDataMongo = new CharacterData(uuid, slot, new PlayerMongoData(uuid.toString()), jedis);
                Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> callback.onQueryComplete(characterDataMongo));
            }
        });
    }

}
