package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class TitleData implements SessionData {

    private static final String DATA_SECTION_UNLOCKED_TITLES = "unlockedTitles";
    public static final String DATA_SECTION_PREFIX = "prefix";
    public static final String DATA_SECTION_SUFFIX = "suffix";
    private final UUID uuid;
    private String prefix;
    private String suffix;
    private final Set<String> unlockedTitles;

    /**
     * Build the player's title data from mongo if it exists. Otherwise, generate blank values.
     * Then write to jedis and memory
     *
     * @param uuid            of the player to save
     * @param playerMongoData of the player
     * @param jedis           the jedis resource
     */
    public TitleData(UUID uuid, PlayerMongoData playerMongoData, Jedis jedis) {
        this.uuid = uuid;
        this.prefix = playerMongoData.get("title." + DATA_SECTION_PREFIX, String.class) != null ? playerMongoData.get("title." + DATA_SECTION_PREFIX, String.class) : "";
        this.suffix = playerMongoData.get("title." + DATA_SECTION_SUFFIX, String.class) != null ? playerMongoData.get("title." + DATA_SECTION_SUFFIX, String.class) : "";
        this.unlockedTitles = getUnlockedTitlesFromMongo(playerMongoData); // get from mongo
        writeTitleDataToJedis(jedis);
        RunicCore.getTitleManager().getTitleDataMap().put(uuid, this);
    }

    /**
     * Build the player's title data from redis, then add to memory
     *
     * @param uuid  of the player to save
     * @param jedis the jedis resource
     */
    public TitleData(UUID uuid, Jedis jedis) {
        this.uuid = uuid;
        this.prefix = jedis.get(uuid + ":" + DATA_SECTION_PREFIX);
        this.suffix = jedis.get(uuid + ":" + DATA_SECTION_SUFFIX);
        this.unlockedTitles = getUnlockedTitlesFromJedis(jedis);
        RunicCore.getTitleManager().getTitleDataMap().put(uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified prefix.
     * Also adds the prefix to the set (which ignores duplicates), to ensure it is saved as 'unlocked'
     *
     * @param prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.unlockedTitles.add(prefix); // insurance
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified suffix.
     * Also adds the suffix to the set (which ignores duplicates), to ensure it is saved as 'unlocked'
     *
     * @param suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.unlockedTitles.add(suffix); // insurance
    }

    public Set<String> getUnlockedTitles() {
        return unlockedTitles;
    }

    /**
     * Builds our in-memory list of unlocked titles from the jedis resource, using the list index method
     *
     * @param jedis the jedis resource
     * @see <a href="https://redis.io/commands/lindex/">redis.io</a>
     */
    private Set<String> getUnlockedTitlesFromJedis(Jedis jedis) {
        final String key = this.uuid + ":" + DATA_SECTION_UNLOCKED_TITLES;
        Set<String> unlockedTitles = new HashSet<>();
        for (int i = 0; i < jedis.llen(key); i++) {
            unlockedTitles.add(jedis.lindex(key, i));
        }
        return unlockedTitles;
    }

    /**
     * @param playerMongoData
     * @return
     */
    private Set<String> getUnlockedTitlesFromMongo(PlayerMongoData playerMongoData) {
        PlayerMongoDataSection playerMongoDataSection = (PlayerMongoDataSection) playerMongoData.getSection("title." + DATA_SECTION_UNLOCKED_TITLES);
        return new HashSet<>(playerMongoDataSection.getKeys());
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    /**
     * Saves the current prefix and suffix to jedis, and keeps track of a list of all unlocked titles
     *
     * @param jedis the jedis resource
     */
    public void writeTitleDataToJedis(Jedis jedis) {
        String uuid = String.valueOf(this.uuid);
        jedis.set(uuid + ":" + DATA_SECTION_PREFIX, this.prefix);
        jedis.set(uuid + ":" + DATA_SECTION_SUFFIX, this.suffix);
        for (String unlockedTitle : this.unlockedTitles) {
            jedis.lpush(uuid + ":" + DATA_SECTION_UNLOCKED_TITLES, unlockedTitle);
        }
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        try {
            playerMongoData.set("title." + DATA_SECTION_PREFIX, this.prefix);
            playerMongoData.set("title." + DATA_SECTION_SUFFIX, this.suffix);
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: There was a problem saving title data to mongo!");
            e.printStackTrace();
        }
    }
}
