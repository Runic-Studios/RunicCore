package com.runicrealms.plugin.model;

import java.util.HashMap;
import java.util.Map;

public class OutlawInfo implements JedisSerializable {
    private final boolean outlawEnabled;
    private final int outlawRating;

    /**
     * A container of outlaw info used to load a player character profile
     *
     * @param outlawEnabled whether the character is flagged as an outlaw
     * @param outlawRating  the rating of the outlaw player
     */
    public OutlawInfo(boolean outlawEnabled, int outlawRating) {
        this.outlawEnabled = outlawEnabled;
        this.outlawRating = outlawRating;
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
    public <T> T fromMap(String key, Class<T> type) {
        return null;
    }
}
