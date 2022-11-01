package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.*;

public class ProfessionData implements SessionData {
    public static final List<String> FIELDS = new ArrayList<String>() {{
        add(CharacterField.PROF_NAME.getField());
        add(CharacterField.PROF_EXP.getField());
        add(CharacterField.PROF_LEVEL.getField());
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
     * @param uuid  of the player
     * @param slot  of the character
     * @param jedis the jedis resource
     */
    public ProfessionData(UUID uuid, int slot, Jedis jedis) {
        this.uuid = uuid;
        Map<String, String> fieldsMap = getDataMapFromJedis(jedis, slot);
        this.profName = fieldsMap.get(CharacterField.PROF_NAME.getField());
        this.profLevel = Integer.parseInt(fieldsMap.get(CharacterField.PROF_LEVEL.getField()));
        this.profExp = Integer.parseInt(fieldsMap.get(CharacterField.PROF_EXP.getField()));
    }

    public UUID getUuid() {
        return this.uuid;
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
            put(CharacterField.PROF_NAME.getField(), profName);
            put(CharacterField.PROF_LEVEL.getField(), String.valueOf(profLevel));
            put(CharacterField.PROF_EXP.getField(), String.valueOf(profExp));
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
        character.set("prof.name", this.profName);
        character.set("prof.level", this.profLevel);
        character.set("prof.exp", this.profExp);
        return playerMongoData;
    }
}
