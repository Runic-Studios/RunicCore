package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;

import java.util.HashMap;
import java.util.Map;

public class ClassInfo implements JedisSerializable {

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
    public ClassInfo(ClassEnum classType, int exp, int level) {
        this.classType = classType;
        this.exp = exp;
        this.level = level;
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
}
