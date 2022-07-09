package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoDataSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfessionData implements JedisSerializable {
    static List<String> fields = new ArrayList<String>() {{
        add("profName");
        add("profExp");
        add("profLevel");
    }};
    private final String profName;
    private final int profExp;
    private final int profLevel;

    /**
     * A container of profession info used to load a player character profile, built from mongo
     *
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public ProfessionData(PlayerMongoDataSection character) {
        this.profName = character.get("prof.name", String.class);
        this.profLevel = character.get("prof.level", Integer.class);
        this.profExp = character.get("prof.exp", Integer.class);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param fields a map of key-value pairs from redis
     */
    public ProfessionData(Map<String, String> fields) {
        this.profName = fields.get("profName");
        this.profLevel = Integer.parseInt(fields.get("profExp"));
        this.profExp = Integer.parseInt(fields.get("profLevel"));
    }

    public static List<String> getFields() {
        return fields;
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

    @Override
    public void writeToMongo(PlayerMongoDataSection character) {
        character.set("prof.name", this.profName);
        character.set("prof.level", this.profLevel);
        character.set("prof.exp", this.profExp);
    }
}
