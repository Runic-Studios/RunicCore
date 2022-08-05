package com.runicrealms.plugin.model;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.redis.RedisField;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class BaseCharacterData implements SessionData {
    static List<String> fields = new ArrayList<String>() {{
        add(RedisField.SLOT.getField());
        add(RedisField.CURRENT_HEALTH.getField());
        add(RedisField.STORED_HUNGER.getField());
        add(RedisField.PLAYER_UUID.getField());
        add(RedisField.LOCATION.getField());
    }};

    private final int slot;
    private final int currentHp;
    private final int storedHunger;
    private final UUID uuid;
    private final Location location;

    /**
     * A container of basic info used to load a player character profile, built from the player's session data values
     *
     * @param slot         the slot of the character (1 for first created profile)
     * @param currentHp    the current health of the character
     * @param storedHunger the current hunger of the character
     * @param uuid         the uuid of the player
     * @param location     the location of the character
     */
    public BaseCharacterData(int slot, int currentHp, int storedHunger, UUID uuid, Location location) {
        this.slot = slot;
        this.currentHp = currentHp;
        this.storedHunger = storedHunger;
        this.uuid = uuid;
        this.location = location;
    }

    /**
     * A container of basic info used to load a player character profile, built from mongo
     *
     * @param uuid      of the player that selected the character profile
     * @param slot      the slot of the character (1 for first created profile)
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public BaseCharacterData(UUID uuid, int slot, PlayerMongoDataSection character) {
        this.uuid = uuid;
        this.slot = slot;
        this.currentHp = character.get("currentHp", Integer.class) != null ? character.get("currentHp", Integer.class) : 200;
        this.storedHunger = character.get("storedHunger", Integer.class) != null ? character.get("storedHunger", Integer.class) : 20;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            this.location = DatabaseUtil.loadLocation(player, character);
        } else {
            this.location = CityLocation.TUTORIAL.getLocation(); // oops!
        }
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param fields a map of key-value pairs from redis
     */
    public BaseCharacterData(UUID uuid, Map<String, String> fields) {
        this.uuid = uuid;
        this.slot = Integer.parseInt(fields.get(RedisField.SLOT.getField()));
        this.currentHp = Integer.parseInt(fields.get(RedisField.CURRENT_HEALTH.getField()));
        this.storedHunger = Integer.parseInt(fields.get(RedisField.STORED_HUNGER.getField()));
        this.location = DatabaseUtil.loadLocation(fields.get(RedisField.LOCATION.getField()));
    }

    public static List<String> getFields() {
        return fields;
    }

    public int getSlot() {
        return slot;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getStoredHunger() {
        return storedHunger;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Returns a map that can be used to set values in redis
     *
     * @return a map of string keys and character info values
     */
    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put("slot", String.valueOf(slot));
            put("currentHp", String.valueOf(currentHp));
            put("storedHunger", String.valueOf(storedHunger));
            put("playerUuid", String.valueOf(uuid));
            put("location", DatabaseUtil.serializeLocation(location));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.set("currentHP", this.currentHp);
        character.set("storedHunger", this.storedHunger);
        DatabaseUtil.saveLocation(character, this.location);
    }
}
