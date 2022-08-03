package com.runicrealms.plugin.model;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.redis.RedisField;

import java.util.*;

public class ProfessionData implements JedisSerializable {
    static List<String> fields = new ArrayList<String>() {{
        add(RedisField.PROF_NAME.getField());
        add(RedisField.PROF_EXP.getField());
        add(RedisField.PROF_LEVEL.getField());
    }};
    private final UUID uuid;
    private final String profName;
    private final int profLevel;
    private final int profExp;

    /**
     * A container of class info used to load a player character profile
     *
     * @param uuid      of the player
     * @param profName  the name of the profession
     * @param profLevel the level of the profession
     * @param profExp   the exp of the profession
     */
    public ProfessionData(UUID uuid, String profName, int profLevel, int profExp) {
        this.uuid = uuid;
        this.profName = profName;
        this.profLevel = profLevel;
        this.profExp = profExp;
    }

    /**
     * A container of profession info used to load a player character profile, built from mongo
     *
     * @param uuid      of the player
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public ProfessionData(UUID uuid, PlayerMongoDataSection character) {
        this.uuid = uuid;
        this.profName = character.get("prof.name", String.class);
        this.profLevel = character.get("prof.level", Integer.class);
        this.profExp = character.get("prof.exp", Integer.class);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param uuid   of the player
     * @param fields a map of key-value pairs from redis
     */
    public ProfessionData(UUID uuid, Map<String, String> fields) {
        this.uuid = uuid;
        this.profName = fields.get(RedisField.PROF_NAME.getField());
        this.profLevel = Integer.parseInt(fields.get(RedisField.PROF_LEVEL.getField()));
        this.profExp = Integer.parseInt(fields.get(RedisField.PROF_EXP.getField()));
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
            put(RedisField.PROF_NAME.getField(), profName);
            put(RedisField.PROF_LEVEL.getField(), String.valueOf(profLevel));
            put(RedisField.PROF_EXP.getField(), String.valueOf(profExp));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(RunicCoreAPI.getCharacterSlot(uuid));
        character.set("prof.name", this.profName);
        character.set("prof.level", this.profLevel);
        character.set("prof.exp", this.profExp);
    }
}
