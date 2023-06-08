package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Intermediary object used to read data from mongo or redis and then store data back in redis and mongo
 * Stored during playtime to update session data
 * Saved during MongoSaveEvent
 *
 * @author Skyfallin
 */
@Document(collection = "core")
@SuppressWarnings("unused")
public class CorePlayerData implements SessionDataMongo {
    @Id
    private ObjectId id;
    @Field("playerUuid")
    private UUID uuid;
    private LocalDate lastLoginDate;
    private String guild = "";
    private HashMap<Integer, CoreCharacterData> coreCharacterDataMap = new HashMap<>();
    private Map<Integer, Map<SkillTreePosition, SkillTreeData>> skillTreeDataMap = new HashMap<>();
    private Map<Integer, SpellData> spellDataMap = new HashMap<>();
    private TitleData titleData = new TitleData();

    @SuppressWarnings("unused")
    public CorePlayerData() {
        // Default constructor for Spring
    }

    /**
     * This is our central data object for the RunicCore plugin that reads/writes to redis and mongo
     *
     * @param id                   of the document (to prevent creating multiple entities)
     * @param uuid                 of the player
     * @param lastLoginDate        of the player
     * @param coreCharacterDataMap a map keyed by character slot
     * @param skillTreeDataMap     a skill map keyed by character slot
     * @param spellDataMap         a spell map keyed by character slot
     * @param titleData            the player's title data
     */
    public CorePlayerData(
            ObjectId id,
            UUID uuid,
            LocalDate lastLoginDate,
            HashMap<Integer, CoreCharacterData> coreCharacterDataMap,
            Map<Integer, Map<SkillTreePosition, SkillTreeData>> skillTreeDataMap,
            HashMap<Integer, SpellData> spellDataMap,
            TitleData titleData) {
        this.id = id;
        this.uuid = uuid;
        this.lastLoginDate = lastLoginDate;
        this.coreCharacterDataMap = coreCharacterDataMap;
        this.skillTreeDataMap = skillTreeDataMap;
        this.spellDataMap = spellDataMap;
        this.titleData = titleData;
    }

    /**
     * Build the player's data from redis.
     * Does not load every character. Instead, lazy-loads the characters as-needed
     *
     * @param uuid  of the player that selected the character profile
     * @param jedis the jedis resource
     */
    public CorePlayerData(UUID uuid, Jedis jedis) {
        this.uuid = uuid;
        this.lastLoginDate = LocalDate.now();
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        if (jedis.exists(database + ":" + uuid + ":guild")) {
            this.guild = jedis.get(database + ":" + uuid + ":guild");
        }
        // Load title data from Redis (no lazy loading for TitleData)
        TitleData titleDataRedis = RunicCore.getTitleAPI().checkRedisForTitleData(uuid, jedis);
        if (titleDataRedis != null) {
            this.titleData = titleDataRedis;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CorePlayerData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
        return mongoTemplate.save(this);
    }

    /**
     * Finds the character data matching slot
     *
     * @param slot of the character
     * @return the CharacterData for RunicCore
     */
    public CoreCharacterData getCharacter(int slot) {
        if (coreCharacterDataMap.get(slot) != null) {
            return coreCharacterDataMap.get(slot);
        }
        // Lazy load the characters from redis
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            Set<String> redisCharacterList = RunicDatabase.getAPI().getRedisAPI().getRedisDataSet(uuid, "characterData", jedis);
            boolean dataInRedis = RunicDatabase.getAPI().getRedisAPI().determineIfDataInRedis(redisCharacterList, slot);
            if (dataInRedis) {
                coreCharacterDataMap.put(slot, new CoreCharacterData(uuid, slot, jedis));
                return coreCharacterDataMap.get(slot);
            }
        }
        return null; // Oh-no!
    }

    public Map<Integer, CoreCharacterData> getCoreCharacterDataMap() {
        return coreCharacterDataMap;
    }

    public void setCoreCharacterDataMap(HashMap<Integer, CoreCharacterData> coreCharacterDataMap) {
        this.coreCharacterDataMap = coreCharacterDataMap;
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * A wrapper for all skill-tree related data
     *
     * @param slot     of the character
     * @param position of the skill tree
     * @return the data containing perks, etc.
     */
    public SkillTreeData getSkillTreeData(int slot, SkillTreePosition position) {
        if (this.skillTreeDataMap.get(slot) != null) {
            return this.skillTreeDataMap.get(slot).get(position);
        }
        return null;
    }

    public Map<Integer, Map<SkillTreePosition, SkillTreeData>> getSkillTreeDataMap() {
        return skillTreeDataMap;
    }

    public void setSkillTreeDataMap(Map<Integer, Map<SkillTreePosition, SkillTreeData>> skillTreeDataMap) {
        this.skillTreeDataMap = skillTreeDataMap;
    }

    /**
     * @param slot of the character
     * @return their spell data wrapper
     */
    public SpellData getSpellData(int slot) {
        if (spellDataMap.get(slot) != null) {
            return spellDataMap.get(slot);
        }
        return null;
    }

    public Map<Integer, SpellData> getSpellDataMap() {
        return spellDataMap;
    }

    public void setSpellDataMap(Map<Integer, SpellData> spellDataMap) {
        this.spellDataMap = spellDataMap;
    }

    public TitleData getTitleData() {
        return titleData;
    }

    public void setTitleData(TitleData titleData) {
        this.titleData = titleData;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void writeToJedis(Jedis jedis) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", this.uuid.toString());
        // Inform the server that there is some core data
        jedis.set(database + ":" + uuid + ":hasCoreData", this.uuid.toString());
        jedis.expire(database + ":" + uuid + ":hasCoreData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
        // Write guild
        jedis.set(database + ":" + uuid + ":guild", this.guild);
        jedis.expire(database + ":" + uuid + ":guild", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
        // Save character data
        for (int slot : this.coreCharacterDataMap.keySet()) {
            // Ensure the system knows that there is data in redis
            jedis.sadd(database + ":" + this.uuid + ":characterData", String.valueOf(slot));
            jedis.expire(database + ":" + this.uuid + ":characterData", RunicDatabase.getAPI().getRedisAPI().getExpireTime());
            CoreCharacterData characterData = this.coreCharacterDataMap.get(slot);
            characterData.writeToJedis(this.uuid, jedis, slot);
        }
    }

}
