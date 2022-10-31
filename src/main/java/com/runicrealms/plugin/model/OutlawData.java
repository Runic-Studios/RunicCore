package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.*;

public class OutlawData implements SessionData {
    private static final Map<UUID, Boolean> OUTLAW_DATA_MAP = new HashMap<>();
    static List<String> fields = new ArrayList<String>() {{
        add(CharacterField.OUTLAW_ENABLED.getField());
        add(CharacterField.OUTLAW_RATING.getField());
    }};
    private final UUID uuid;
    private final boolean outlawEnabled;
    private final int outlawRating;

    /**
     * A container of outlaw info used to load a player character profile, built from mongo
     *
     * @param uuid      of the player that selected the character profile
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public OutlawData(UUID uuid, PlayerMongoDataSection character) {
        this.uuid = uuid;
        this.outlawEnabled = character.get("outlaw.enabled", Boolean.class);
        this.outlawRating = character.get("outlaw.rating", Integer.class);
        OUTLAW_DATA_MAP.put(uuid, this.outlawEnabled);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param uuid   of the player that selected the character profile
     * @param fields a map of key-value pairs from redis
     */
    public OutlawData(UUID uuid, Map<String, String> fields) {
        this.uuid = uuid;
        this.outlawEnabled = Boolean.parseBoolean(fields.get(CharacterField.OUTLAW_ENABLED.getField()));
        this.outlawRating = Integer.parseInt(fields.get(CharacterField.OUTLAW_RATING.getField()));
        OUTLAW_DATA_MAP.put(uuid, this.outlawEnabled);
    }

    public static List<String> getFields() {
        return fields;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean isOutlawEnabled() {
        return outlawEnabled;
    }

    public int getOutlawRating() {
        return outlawRating;
    }

    /**
     * Returns a map that can be used to set values in redis
     *
     * @return a map of string keys and character info values
     */
    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put(CharacterField.OUTLAW_ENABLED.getField(), String.valueOf(outlawEnabled));
            put(CharacterField.OUTLAW_RATING.getField(), String.valueOf(outlawRating));
        }};
    }

    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String uuid = String.valueOf(this.uuid);
        String key = uuid + ":character:" + slot[0];
        jedis.hmset(key, this.toMap());
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.set("outlaw.enabled", this.outlawEnabled);
        character.set("outlaw.rating", this.outlawRating);
    }

    /**
     * Used for memoization of outlaw status
     *
     * @return a map of uuid to the status of their outlaw setting
     */
    public static Map<UUID, Boolean> getOutlawDataMap() {
        return OUTLAW_DATA_MAP;
    }
}
