package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.*;

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
        writeToJedis(jedis);
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
        this.unlockedTitles = getDataMapFromJedis(jedis).keySet();
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
     * @param playerMongoData
     * @return
     */
    private Set<String> getUnlockedTitlesFromMongo(PlayerMongoData playerMongoData) {
        if (playerMongoData.get("title." + DATA_SECTION_UNLOCKED_TITLES) != null) {
            PlayerMongoDataSection unlockedTitles = (PlayerMongoDataSection) playerMongoData.getSection("title." + DATA_SECTION_UNLOCKED_TITLES);
            return new HashSet<>(unlockedTitles.getKeys());
        }
        return new HashSet<>();
    }

    @Override
    public Map<String, String> toMap() {
        return null;
    }

    /**
     * Builds our in-memory list of unlocked titles from the jedis resource, using the list index method
     *
     * @param jedis the jedis resource
     * @see <a href="https://redis.io/commands/lindex/">redis.io</a>
     */
    @Override
    public Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot) {

        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(ClassData.getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);

        final String key = this.uuid + ":" + DATA_SECTION_UNLOCKED_TITLES;
        Set<String> unlockedTitles = new HashSet<>();
        for (int i = 0; i < jedis.llen(key); i++) {
            unlockedTitles.add(jedis.lindex(key, i));
        }

        int i = 0;
        for (String title : unlockedTitles) {
            fieldsMap.put(title, fieldsToArray[i]);
            i++;
        }
        return fieldsMap;
    }

    /**
     * Saves the current prefix and suffix to jedis, and keeps track of a list of all unlocked titles
     * Slot param is not used, since this account-wide data
     *
     * @param jedis the jedis resource
     */
    @Override
    public void writeToJedis(Jedis jedis, int... slot) {
        String uuid = String.valueOf(this.uuid);
        jedis.set(uuid + ":" + DATA_SECTION_PREFIX, this.prefix);
        jedis.set(uuid + ":" + DATA_SECTION_SUFFIX, this.suffix);
        jedis.del(uuid + ":" + DATA_SECTION_UNLOCKED_TITLES); // reset keys
        for (String unlockedTitle : this.unlockedTitles) {
            jedis.lpush(uuid + ":" + DATA_SECTION_UNLOCKED_TITLES, unlockedTitle);
        }
    }

    @Override
    public PlayerMongoData writeToMongo(PlayerMongoData playerMongoData, Jedis jedis, int... slot) {
        try {
            playerMongoData.set("title." + DATA_SECTION_PREFIX, jedis.get(uuid + ":" + DATA_SECTION_PREFIX));
            playerMongoData.set("title." + DATA_SECTION_SUFFIX, jedis.get(uuid + ":" + DATA_SECTION_SUFFIX));
            playerMongoData.remove("title." + DATA_SECTION_UNLOCKED_TITLES);
            PlayerMongoDataSection titlesSection = (PlayerMongoDataSection) playerMongoData.getSection("title." + DATA_SECTION_UNLOCKED_TITLES);
            for (String title : getDataMapFromJedis(jedis).keySet()) {
                titlesSection.set(title, "true");
            }
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: There was a problem saving title data to mongo!");
            e.printStackTrace();
        }
        return playerMongoData;
    }
}
