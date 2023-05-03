package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SettingsData implements SessionDataRedis {
    public static final String DATA_SECTION_SETTINGS = "settings:castMenuEnabled";
    private boolean castMenuEnabled = true;

    public SettingsData() {
        // Default constructor for Spring
    }

    /**
     * Build the player's settings data from redis, then add to memory
     *
     * @param uuid  of the player to lookup
     * @param jedis the jedis resource
     */
    public SettingsData(UUID uuid, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        this.castMenuEnabled = Boolean.parseBoolean(jedis.get(database + ":" + uuid + ":" + DATA_SECTION_SETTINGS));
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        return null;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... slot) {
        return null;
    }

    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... slot) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":markedForSave:core", uuid.toString());
        jedis.set(database + ":" + uuid + ":" + DATA_SECTION_SETTINGS, String.valueOf(this.castMenuEnabled));
        jedis.expire(database + ":" + uuid + ":" + DATA_SECTION_SETTINGS, RunicCore.getRedisAPI().getExpireTime());
    }

    public boolean isCastMenuEnabled() {
        return castMenuEnabled;
    }

    public void setCastMenuEnabled(boolean castMenuEnabled) {
        this.castMenuEnabled = castMenuEnabled;
    }
}
