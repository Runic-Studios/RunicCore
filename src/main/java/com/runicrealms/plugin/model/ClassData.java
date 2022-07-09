package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoDataSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassData implements JedisSerializable {
    static List<String> fields = new ArrayList<String>() {{
        add("classType");
        add("exp");
        add("level");
    }};
    private final ClassEnum classType;
    private final int exp;
    private final int level;

    /**
     * A container of class info used to load a player character profile
     *
     * @param classType the class of the character (e.g., Cleric)
     * @param exp       the exp of the character
     * @param level     the level of the character
     */
    public ClassData(ClassEnum classType, int exp, int level) {
        this.classType = classType;
        this.exp = exp;
        this.level = level;
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
    public ClassData(Map<String, String> fields) {
        this.classType = ClassEnum.getFromName(fields.get("slot"));
        this.exp = Integer.parseInt(fields.get("exp"));
        this.level = Integer.parseInt(fields.get("level"));
    }

    public static List<String> getFields() {
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
            put("classType", classType.getName());
            put("exp", String.valueOf(exp));
            put("level", String.valueOf(level));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoDataSection character) {
        character.set("class.name", this.classType.getName());
        character.set("class.level", this.getLevel());
        character.set("class.exp", this.getExp());
    }
}
