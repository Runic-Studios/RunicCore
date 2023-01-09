package com.runicrealms.plugin.model;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.database.DatabaseHelper;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.*;

public class BaseCharacterData implements SessionData {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(CharacterField.SLOT.getField());
        add(CharacterField.CURRENT_HEALTH.getField());
        add(CharacterField.STORED_HUNGER.getField());
        add(CharacterField.PLAYER_UUID.getField());
        add(CharacterField.LOCATION.getField());
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
            this.location = DatabaseHelper.loadLocationFromSerializedString(character);
        } else {
            this.location = CityLocation.TUTORIAL.getLocation(); // oops!
        }
    }

    /**
     * A container of basic info used to load a player character profile, built from jedis
     *
     * @param uuid  of the player that selected the character profile
     * @param slot  the slot of the character (1 for first created profile)
     * @param jedis the jedis resource
     */
    public BaseCharacterData(UUID uuid, int slot, Jedis jedis) {
        this.uuid = uuid;
        Map<String, String> fieldsMap = getDataMapFromJedis(jedis, slot);
        this.slot = Integer.parseInt(fieldsMap.get(CharacterField.SLOT.getField()));
        this.currentHp = Integer.parseInt(fieldsMap.get(CharacterField.CURRENT_HEALTH.getField()));
        this.storedHunger = Integer.parseInt(fieldsMap.get(CharacterField.STORED_HUNGER.getField()));
        this.location = DatabaseHelper.loadLocationFromSerializedString(fieldsMap.get(CharacterField.LOCATION.getField()));
    }

    public int getCurrentHp() {
        return currentHp;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        List<String> values = jedis.hmget(uuid + ":character:" + slot[0], fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        return fieldsMap;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
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
            put("location", DatabaseHelper.serializeLocation(location));
        }};
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String uuid = String.valueOf(this.uuid);
        String key = uuid + ":character:" + this.slot;
        jedis.hmset(key, this.toMap());
    }

    @Override
    public MongoData writeToMongo(MongoData mongoData, int... slot) {
        PlayerMongoData playerMongoData = (PlayerMongoData) mongoData;
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.set(CharacterField.CURRENT_HEALTH.getField(), this.currentHp);
        character.set(CharacterField.STORED_HUNGER.getField(), this.storedHunger);
        DatabaseHelper.saveLocation(character, this.location);
        return playerMongoData;
    }

    public Location getLocation() {
        return location;
    }

    public int getSlot() {
        return slot;
    }

    public int getStoredHunger() {
        return storedHunger;
    }

    public UUID getUuid() {
        return uuid;
    }
}
