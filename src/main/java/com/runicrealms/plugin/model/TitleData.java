package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class TitleData implements SessionData {

    public static final String DATA_SECTION_PREFIX = "prefix";
    public static final String DATA_SECTION_SUFFIX = "suffix";
    private final UUID uuid;
    private String prefix;
    private String suffix;

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
        writeTitleDataToJedis(jedis);
        RunicCore.getTitleManager().getTitleDataMap().put(uuid, this);
    }

    /**
     * @param uuid
     * @param jedis
     */
    public TitleData(UUID uuid, Jedis jedis) {
        this.uuid = uuid;
        this.prefix = jedis.get(uuid + ":" + DATA_SECTION_PREFIX);
        this.suffix = jedis.get(uuid + ":" + DATA_SECTION_SUFFIX);
        RunicCore.getTitleManager().getTitleDataMap().put(uuid, this);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    public void writeTitleDataToJedis(Jedis jedis) {
        String uuid = String.valueOf(this.uuid);
        jedis.set(uuid + ":" + DATA_SECTION_PREFIX, this.prefix);
        jedis.set(uuid + ":" + DATA_SECTION_SUFFIX, this.suffix);
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
