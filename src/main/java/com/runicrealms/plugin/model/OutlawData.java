package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.redis.RedisField;

import java.util.*;

public class OutlawData implements SessionData {
    static List<String> fields = new ArrayList<String>() {{
        add(RedisField.OUTLAW_ENABLED.getField());
        add(RedisField.OUTLAW_RATING.getField());
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
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param uuid   of the player that selected the character profile
     * @param fields a map of key-value pairs from redis
     */
    public OutlawData(UUID uuid, Map<String, String> fields) {
        this.uuid = uuid;
        this.outlawEnabled = Boolean.parseBoolean(fields.get(RedisField.OUTLAW_ENABLED.getField()));
        this.outlawRating = Integer.parseInt(fields.get(RedisField.OUTLAW_RATING.getField()));
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
            put(RedisField.OUTLAW_ENABLED.getField(), String.valueOf(outlawEnabled));
            put(RedisField.OUTLAW_RATING.getField(), String.valueOf(outlawRating));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        character.set("outlaw.enabled", this.outlawEnabled);
        character.set("outlaw.rating", this.outlawRating);
    }
}
