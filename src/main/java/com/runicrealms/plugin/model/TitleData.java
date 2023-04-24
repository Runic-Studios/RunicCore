package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import redis.clients.jedis.Jedis;

import java.util.*;

public class TitleData implements SessionDataRedis {
    public static final String DATA_SECTION_PREFIX = "title:prefix";
    public static final String DATA_SECTION_SUFFIX = "title:suffix";
    private static final String DATA_SECTION_UNLOCKED_TITLES = "unlockedTitles";
    private Set<String> unlockedTitles = new HashSet<>();
    private String prefix = "";
    private String suffix = "";

    /**
     * Constructor for Spring and new players
     */
    @SuppressWarnings("unused")
    public TitleData() {
        // Default constructor for Spring
    }

    /**
     * Build the player's title data from redis, then add to memory
     *
     * @param uuid  of the player to save
     * @param jedis the jedis resource
     */
    public TitleData(UUID uuid, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        this.prefix = jedis.get(database + ":" + uuid + ":" + DATA_SECTION_PREFIX);
        this.suffix = jedis.get(database + ":" + uuid + ":" + DATA_SECTION_SUFFIX);
        this.unlockedTitles = getDataMapFromJedis(uuid, jedis).keySet();
    }

    /**
     * Builds our in-memory list of unlocked titles from the jedis resource, using the list index method
     *
     * @param jedis the jedis resource
     * @see <a href="https://redis.io/commands/lindex/">redis.io</a>
     */
    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        final String key = uuid + ":" + DATA_SECTION_UNLOCKED_TITLES;
        Set<String> unlockedTitles = new HashSet<>();
        for (int i = 0; i < jedis.llen(database + ":" + key); i++) {
            unlockedTitles.add(jedis.lindex(database + ":" + key, i));
        }

        int i = 0;
        for (String title : unlockedTitles) {
            fieldsMap.put(title, i + "");
            i++;
        }
        return fieldsMap;
    }

    @Override
    public List<String> getFields() {
        return null;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... slot) {
        return null;
    }

    /**
     * Saves the current prefix and suffix to jedis, and keeps track of a list of all unlocked titles
     * Slot param is not used, since this account-wide data
     *
     * @param jedis the jedis resource
     */
    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... ignored) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":markedForSave:core", uuid.toString());
        if (this.prefix != null) {
            jedis.set(database + ":" + uuid + ":" + DATA_SECTION_PREFIX, this.prefix);
            jedis.expire(database + ":" + uuid + ":" + DATA_SECTION_PREFIX, RunicCore.getRedisAPI().getExpireTime());
        }
        if (this.suffix != null) {
            jedis.set(database + ":" + uuid + ":" + DATA_SECTION_SUFFIX, this.suffix);
            jedis.expire(database + ":" + uuid + ":" + DATA_SECTION_SUFFIX, RunicCore.getRedisAPI().getExpireTime());
        }
        jedis.del(database + ":" + uuid + ":" + DATA_SECTION_UNLOCKED_TITLES); // reset keys
        for (String unlockedTitle : this.unlockedTitles) {
            jedis.lpush(database + ":" + uuid + ":" + DATA_SECTION_UNLOCKED_TITLES, unlockedTitle);
        }
        jedis.expire(database + ":" + uuid + ":" + DATA_SECTION_UNLOCKED_TITLES, RunicCore.getRedisAPI().getExpireTime());
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified prefix
     *
     * @param prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified suffix
     *
     * @param suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Set<String> getUnlockedTitles() {
        return unlockedTitles;
    }

    public void setUnlockedTitles(Set<String> unlockedTitles) {
        this.unlockedTitles = unlockedTitles;
    }

    private void unlockTitle(String title) {
        this.unlockedTitles.add(title);
    }

}
