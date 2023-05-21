package com.runicrealms.plugin.model;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.rdb.DatabaseHelper;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.plugin.rdb.model.SessionDataRedis;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoreCharacterData implements SessionDataRedis {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(CharacterField.CLASS_TYPE.getField());
        add(CharacterField.CLASS_EXP.getField());
        add(CharacterField.CLASS_LEVEL.getField());
        add(CharacterField.SLOT.getField());
        add(CharacterField.CURRENT_HEALTH.getField());
        add(CharacterField.STORED_HUNGER.getField());
        add(CharacterField.PLAYER_UUID.getField());
        add(CharacterField.LOCATION.getField());
    }};
    private CharacterClass classType;
    private int level;
    private int exp;
    private int currentHp;
    private Location location;

    @SuppressWarnings("unused")
    public CoreCharacterData() {
        // Default constructor for Spring
    }

    /**
     * A container of class info used to load a player's character profile
     *
     * @param classType the class of the character (e.g., Cleric)
     * @param level     the level of the character
     * @param exp       the exp of the character
     * @param currentHp the stored health of the character
     * @param location  the last location of the character
     */
    public CoreCharacterData(CharacterClass classType, int level, int exp, int currentHp, Location location) {
        this.classType = classType;
        this.level = level;
        this.exp = exp;
        this.currentHp = currentHp;
        this.location = location;
    }

    /**
     * A container of basic info used to load a player character profile, built from jedis
     *
     * @param uuid  of the player that selected the character profile
     * @param slot  the slot of the character (1 for first created profile)
     * @param jedis the jedis resource
     */
    public CoreCharacterData(UUID uuid, int slot, Jedis jedis) {
        Map<String, String> fieldsMap = getDataMapFromJedis(uuid, jedis, slot);
        this.currentHp = Integer.parseInt(fieldsMap.get(CharacterField.CURRENT_HEALTH.getField()));
        try {
            this.location = DatabaseHelper.loadLocationFromSerializedString(fieldsMap.get(CharacterField.LOCATION.getField()));
        } catch (IllegalStateException exception) {
            exception.printStackTrace();
            this.location = CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK); // return hearth location
        }
        this.classType = CharacterClass.getFromName(fieldsMap.get(CharacterField.CLASS_TYPE.getField()));
        this.exp = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_EXP.getField()));
        this.level = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_LEVEL.getField()));
    }

    /**
     * Attempts to populate the document for a new character slot with default values
     *
     * @param corePlayerData of parent object of player data who created character
     * @param className      the name of the class
     * @param slot           the slot of the character
     */
    public static void createCoreCharacterData(CorePlayerData corePlayerData, String className, Integer slot, final WriteCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            Location location = CityLocation.TUTORIAL.getLocation();
            CoreCharacterData coreCharacterData = new CoreCharacterData
                    (
                            CharacterClass.getFromName(className),
                            0,
                            0,
                            HealthUtils.getBaseHealth(),
                            location
                    );
            corePlayerData.getCoreCharacterDataMap().put(slot, coreCharacterData);
            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                corePlayerData.writeToJedis(jedis);
            }
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), callback::onWriteComplete);
        });
    }

    public CharacterClass getClassType() {
        return this.classType;
    }

    public void setClassType(CharacterClass classType) {
        this.classType = classType;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        List<String> values = jedis.hmget(database + ":" + uuid + ":character:" + slot[0], fieldsToArray);
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
    public Map<String, String> toMap(UUID uuid, int... slot) {
        return new HashMap<>() {{
            put(CharacterField.CLASS_TYPE.getField(), classType.getName());
            put(CharacterField.CLASS_EXP.getField(), String.valueOf(exp));
            put(CharacterField.CLASS_LEVEL.getField(), String.valueOf(level));
            put(CharacterField.CURRENT_HEALTH.getField(), String.valueOf(currentHp));
            put(CharacterField.PLAYER_UUID.getField(), String.valueOf(uuid));
            put(CharacterField.LOCATION.getField(), DatabaseHelper.serializeLocation(location));
        }};
    }

    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... slot) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", uuid.toString());
        // Inform the server that there is some core data
        jedis.set(database + ":" + uuid + ":hasCoreData", uuid.toString());
        jedis.expire(database + ":" + uuid + ":hasCoreData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
        // Inform the server that there is some character data
        jedis.sadd(database + ":" + uuid + ":characterData", String.valueOf(slot[0]));
        jedis.expire(database + ":" + uuid + ":characterData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
        String key = uuid + ":character:" + slot[0];
        jedis.hmset(database + ":" + key, this.toMap(uuid));
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
