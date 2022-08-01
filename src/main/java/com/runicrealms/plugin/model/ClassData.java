package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.redis.RedisField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassData implements JedisSerializable {
    static List<RedisField> fields = new ArrayList<RedisField>() {{
        add(RedisField.CLASS_TYPE);
        add(RedisField.CLASS_EXP);
        add(RedisField.CLASS_LEVEL);
    }};
    private final ClassEnum classType;
    private final int level;
    private final int exp;

    /**
     * A container of class info used to load a player character profile
     *
     * @param classType the class of the character (e.g., Cleric)
     * @param level     the level of the character
     * @param exp       the exp of the character
     */
    public ClassData(ClassEnum classType, int level, int exp) {
        this.classType = classType;
        this.level = level;
        this.exp = exp;
    }

    /**
     * A container of class info used to load a player character profile, built from mongo
     *
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public ClassData(PlayerMongoDataSection character) {
        this.classType = ClassEnum.getFromName(character.get("class.name", String.class));
        this.exp = character.get("class.exp", Integer.class);
        this.level = character.get("class.level", Integer.class);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param fields a map of key-value pairs from redis
     */
    public ClassData(Map<RedisField, String> fields) {
        this.classType = ClassEnum.getFromName(fields.get(RedisField.SLOT));
        this.exp = Integer.parseInt(fields.get(RedisField.CLASS_EXP));
        this.level = Integer.parseInt(fields.get(RedisField.CLASS_LEVEL));
    }

    public static List<RedisField> getFields() {
        return fields;
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

    /**
     * Returns a map that can be used to set values in redis
     *
     * @return a map of string keys and character info values
     */
    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put(RedisField.CLASS_TYPE.getField(), classType.getName());
            put(RedisField.CLASS_EXP.getField(), String.valueOf(exp));
            put(RedisField.CLASS_LEVEL.getField(), String.valueOf(level));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoDataSection character) {
        character.set("class.name", this.classType.getName());
        character.set("class.level", this.getLevel());
        character.set("class.exp", this.getExp());
    }
}
