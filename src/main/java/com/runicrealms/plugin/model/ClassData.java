package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ClassData represents a projection of fields used to speed up the character select screen
 * Instead of loading all player characters at login, we just project the fields we need from mongo
 * (Or load directly from redis)
 *
 * @author Skyfallin
 */
public class ClassData implements SessionDataRedis {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(CharacterField.CLASS_TYPE.getField());
        add(CharacterField.CLASS_EXP.getField());
        add(CharacterField.CLASS_LEVEL.getField());
    }};
    private final UUID uuid;
    private final CharacterClass classType;
    private final int level;
    private final int exp;

    /**
     * A container of class info used to load a player's character profile
     *
     * @param uuid      of the player
     * @param classType the class of the character (e.g., Cleric)
     * @param level     the level of the character
     * @param exp       the exp of the character
     */
    public ClassData(UUID uuid, CharacterClass classType, int level, int exp) {
        this.uuid = uuid;
        this.classType = classType;
        this.level = level;
        this.exp = exp;
    }

    /**
     * A container of class info used to load a player's character profile from jedis
     *
     * @param uuid  of the player
     * @param slot  of the character
     * @param jedis the jedis resource
     */
    public ClassData(UUID uuid, int slot, Jedis jedis) {
        Map<String, String> fieldsMap = new HashMap<>();
        String[] fieldsToArray = FIELDS.toArray(new String[0]);
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        List<String> values = jedis.hmget(database + ":" + uuid + ":character:" + slot, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        this.uuid = uuid;
        this.classType = CharacterClass.getFromName(fieldsMap.get(CharacterField.CLASS_TYPE.getField()));
        this.exp = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_EXP.getField()));
        this.level = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_LEVEL.getField()));
    }

    public CharacterClass getClassType() {
        return this.classType;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
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
        }};
    }

    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        String key = uuid + ":character:" + slot[0];
        jedis.hmset(database + ":" + key, this.toMap(uuid));
        jedis.expire(database + ":" + key, RunicCore.getRedisAPI().getExpireTime());
    }

    public int getExp() {
        return this.exp;
    }

    public int getLevel() {
        return this.level;
    }

    public UUID getUuid() {
        return this.uuid;
    }

}
