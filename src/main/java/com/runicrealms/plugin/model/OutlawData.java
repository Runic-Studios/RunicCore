package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoDataSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutlawData implements JedisSerializable {
    static List<String> fields = new ArrayList<String>() {{
        add("outlawEnabled");
        add("outlawRating");
    }};
    private final boolean outlawEnabled;
    private final int outlawRating;

    /**
     * A container of outlaw info used to load a player character profile, built from mongo
     *
     * @param character a PlayerMongoDataSection corresponding to the chosen slot
     */
    public OutlawData(PlayerMongoDataSection character) {
        this.outlawEnabled = character.get("outlaw.enabled", Boolean.class);
        this.outlawRating = character.get("outlaw.rating", Integer.class);
    }

    /**
     * A container of basic info used to load a player character profile, built from redis
     *
     * @param fields a map of key-value pairs from redis
     */
    public OutlawData(Map<String, String> fields) {
        this.outlawEnabled = Boolean.parseBoolean(fields.get("outlawEnabled"));
        this.outlawRating = Integer.parseInt(fields.get("outlawRating"));
    }

    public static List<String> getFields() {
        return fields;
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
            put("outlawEnabled", String.valueOf(outlawEnabled));
            put("outlawRating", String.valueOf(outlawRating));
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoDataSection character) {
        character.set("outlaw.enabled", this.outlawEnabled);
        character.set("outlaw.rating", this.outlawRating);
    }
}
