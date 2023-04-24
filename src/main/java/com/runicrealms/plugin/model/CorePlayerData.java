package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.*;

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
    private HashMap<Integer, CoreCharacterData> coreCharacterDataMap = new HashMap<>();
    private HashMap<Integer, HashMap<SkillTreePosition, SkillTreeData>> skillTreeDataMap = new HashMap<>();
    private HashMap<Integer, SpellData> spellDataMap = new HashMap<>();
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
            HashMap<Integer, HashMap<SkillTreePosition, SkillTreeData>> skillTreeDataMap,
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
        // Load title data from Redis (no lazy loading for TitleData)
        TitleData titleDataRedis = RunicCore.getTitleAPI().checkRedisForTitleData(uuid, jedis);
        if (titleDataRedis != null) {
            this.titleData = titleDataRedis;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CorePlayerData addDocumentToMongo() {
        MongoTemplate mongoTemplate = RunicCore.getDataAPI().getMongoTemplate();
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
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            Set<String> redisCharacterList = RunicCore.getRedisAPI().getRedisDataSet(uuid, "characterData", jedis);
            boolean dataInRedis = RunicCore.getRedisAPI().determineIfDataInRedis(redisCharacterList, slot);
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

    /**
     * ?
     *
     * @param slot
     * @return
     */
    public HashMap<SkillTreePosition, SkillTreeData> getSkillTreeData(int slot) {
        if (skillTreeDataMap.get(slot) != null) {
            return skillTreeDataMap.get(slot);
        }
        // Lazy load the SkillTreeData from Redis (if it exists)
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            SkillTreeData first = RunicCore.getSkillTreeAPI().checkRedisForSkillTreeData(uuid, slot, SkillTreePosition.FIRST, jedis);
            SkillTreeData second = RunicCore.getSkillTreeAPI().checkRedisForSkillTreeData(uuid, slot, SkillTreePosition.SECOND, jedis);
            SkillTreeData third = RunicCore.getSkillTreeAPI().checkRedisForSkillTreeData(uuid, slot, SkillTreePosition.THIRD, jedis);
            List<SkillTreeData> list = new ArrayList<SkillTreeData>() {{
                add(first);
                add(second);
                add(third);
            }};
            for (SkillTreeData data : list) {
                if (data != null) {
                    skillTreeDataMap.computeIfAbsent(slot, k -> new HashMap<>());
                    skillTreeDataMap.get(slot).put(SkillTreePosition.FIRST, first);
                    skillTreeDataMap.get(slot).put(SkillTreePosition.SECOND, second);
                    skillTreeDataMap.get(slot).put(SkillTreePosition.THIRD, third);
                }
            }
            return skillTreeDataMap.get(slot);
        }
    }

    public HashMap<Integer, HashMap<SkillTreePosition, SkillTreeData>> getSkillTreeDataMap() {
        return skillTreeDataMap;
    }

    public void setSkillTreeDataMap(HashMap<Integer, HashMap<SkillTreePosition, SkillTreeData>> skillTreeDataMap) {
        this.skillTreeDataMap = skillTreeDataMap;
    }

    /**
     * ?
     *
     * @param slot
     * @return
     */
    public SpellData getSpellData(int slot) {
        if (spellDataMap.get(slot) != null) {
            return spellDataMap.get(slot);
        }
        // Lazy load the SpellData from Redis (if it exists)
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            SpellData spellData = RunicCore.getSkillTreeAPI().checkRedisForSpellData(uuid, slot, jedis);
            if (spellData != null) {
                spellDataMap.put(slot, new SpellData(uuid, slot, jedis));
                return spellDataMap.get(slot);
            }
        }
        return null; // Oh-no!
    }

    public Map<Integer, SpellData> getSpellDataMap() {
        return spellDataMap;
    }

    public void setSpellDataMap(HashMap<Integer, SpellData> spellDataMap) {
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
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", this.uuid.toString());
        // Inform the server that there is some core data
        jedis.set(database + ":" + uuid + ":hasCoreData", this.uuid.toString());
        jedis.expire(database + ":" + uuid + ":hasCoreData", RunicCore.getRedisAPI().getExpireTime());
        // Save character data
        for (int slot : this.coreCharacterDataMap.keySet()) {
            // Ensure the system knows that there is data in redis
            jedis.sadd(database + ":" + this.uuid + ":characterData", String.valueOf(slot));
            jedis.expire(database + ":" + this.uuid + ":characterData", RunicCore.getRedisAPI().getExpireTime());
            CoreCharacterData characterData = this.coreCharacterDataMap.get(slot);
            characterData.writeToJedis(this.uuid, jedis, slot);
        }
    }

}
