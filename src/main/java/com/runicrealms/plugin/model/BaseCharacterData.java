package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.redis.RedisField;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class BaseCharacterData implements JedisSerializable {
    static List<RedisField> fields = new ArrayList<RedisField>() {{
        add(RedisField.SLOT);
        add(RedisField.CURRENT_HEALTH);
        add(RedisField.STORED_HUNGER);
        add(RedisField.PLAYER_UUID);
        add(RedisField.LOCATION);
    }};

    private final int slot;
    private final int currentHp;
    private final int storedHunger;
    private final UUID playerUuid;
    private final Location location;

    /**
     * A container of basic info used to load a player character profile, built from the player's session data values
     *
     * @param slot         the slot of the character (1 for first created profile)
     * @param currentHp    the current health of the character
     * @param storedHunger the current hunger of the character
     * @param playerUuid   the uuid of the player
     * @param location     the location of the character
     */
    public BaseCharacterData(int slot, int currentHp, int storedHunger, UUID playerUuid, Location location) {
        this.slot = slot;
        this.currentHp = currentHp;
        this.storedHunger = storedHunger;
        this.playerUuid = playerUuid;
        this.location = location;
    }

    /**
     * A container of basic info used to load a player character profile, built from mongo
     *
     * @param player    the player that selected the character profile
     * @param slot      the slot of the character (1 for first created profile)
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public BaseCharacterData(Player player, int slot, PlayerMongoDataSection character) {
        this.slot = slot;
        this.currentHp = character.get("currentHP", Integer.class);
        this.storedHunger = character.get("storedHunger", Integer.class) != null ? character.get("storedHunger", Integer.class) : 20;
        this.playerUuid = player.getUniqueId();
        this.location = DatabaseUtil.loadLocation(player, character);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param fields a map of key-value pairs from redis
     */
    public BaseCharacterData(Map<RedisField, String> fields) {
        this.slot = Integer.parseInt(fields.get(RedisField.SLOT));
        this.currentHp = Integer.parseInt(fields.get(RedisField.CURRENT_HEALTH));
        this.storedHunger = Integer.parseInt(fields.get(RedisField.STORED_HUNGER));
        this.playerUuid = UUID.fromString(fields.get(RedisField.PLAYER_UUID));
        this.location = DatabaseUtil.loadLocation(fields.get(RedisField.LOCATION));
    }

    public static List<RedisField> getFields() {
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

    public UUID getPlayerUuid() {
        return playerUuid;
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
            put("playerUuid", String.valueOf(playerUuid));
            put("location", DatabaseUtil.serializeLocation(location));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoDataSection character) {
        character.set("currentHP", this.currentHp);
        character.set("storedHunger", this.storedHunger);
        DatabaseUtil.saveLocation(character, this.location);
    }
}
