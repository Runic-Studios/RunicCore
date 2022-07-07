package com.runicrealms.plugin.model;

import java.util.HashMap;
import java.util.Map;

public class ProfessionInfo implements JedisSerializable {
    private final String profName;
    private final int profExp;
    private final int profLevel;

    /**
     * A container of profession info used to load a player character profile
     *
     * @param profName  the name of the character's profession
     * @param profExp   the experience of the character's profession
     * @param profLevel the level of the character's profession
     */
    public ProfessionInfo(String profName, int profExp, int profLevel) {
        this.profName = profName;
        this.profExp = profExp;
        this.profLevel = profLevel;
    }

    public String getProfName() {
        return profName;
    }

    public int getProfExp() {
        return profExp;
    }

    public int getProfLevel() {
        return profLevel;
    }

    /**
     * Returns a map that can be used to set values in redis
     *
     * @return a map of string keys and character info values
     */
    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put("profName", profName);
            put("profExp", String.valueOf(profExp));
            put("profLevel", String.valueOf(profLevel));
        }};
    }
}
