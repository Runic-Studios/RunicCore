package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.*;

public class ClassData implements SessionData {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(CharacterField.CLASS_TYPE.getField());
        add(CharacterField.CLASS_EXP.getField());
        add(CharacterField.CLASS_LEVEL.getField());
    }};
    private final UUID uuid;
    private final ClassEnum classType;
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
    public ClassData(UUID uuid, ClassEnum classType, int level, int exp) {
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
        List<String> values = jedis.hmget(uuid + ":character:" + slot, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        this.uuid = uuid;
        this.classType = ClassEnum.getFromName(fieldsMap.get(CharacterField.CLASS_TYPE.getField()));
        this.exp = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_EXP.getField()));
        this.level = Integer.parseInt(fieldsMap.get(CharacterField.CLASS_LEVEL.getField()));
    }

    /**
     * A container of class info used to load a player character profile, built from mongo
     *
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public ClassData(UUID uuid, PlayerMongoDataSection character) {
        this.uuid = uuid;
        this.classType = ClassEnum.getFromName(character.get("class.name", String.class));
        this.exp = character.get("class.exp", Integer.class);
        this.level = character.get("class.level", Integer.class);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param FIELDS a map of key-value pairs from redis
     */
    public ClassData(UUID uuid, Map<String, String> FIELDS) {
        this.uuid = uuid;
        this.classType = ClassEnum.getFromName(FIELDS.get(CharacterField.CLASS_TYPE.getField()));
        this.exp = Integer.parseInt(FIELDS.get(CharacterField.CLASS_EXP.getField()));
        this.level = Integer.parseInt(FIELDS.get(CharacterField.CLASS_LEVEL.getField()));
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public ClassEnum getClassType() {
        return this.classType;
    }

    public int getExp() {
        return this.exp;
    }

    public int getLevel() {
        return this.level;
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
            put(CharacterField.CLASS_TYPE.getField(), classType.getName());
            put(CharacterField.CLASS_EXP.getField(), String.valueOf(exp));
            put(CharacterField.CLASS_LEVEL.getField(), String.valueOf(level));
        }};
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
    public void writeToJedis(Jedis jedis, int... slot) {
        String uuid = String.valueOf(this.uuid);
        String key = uuid + ":character:" + slot[0];
        jedis.hmset(key, this.toMap());
    }

    @Override
    public PlayerMongoData writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.set("class.name", this.classType);
        character.set("class.level", this.level);
        character.set("class.exp", this.exp);
        return playerMongoData;
    }
}
