package com.runicrealms.plugin.model;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.SessionDataMongo;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
    @Nullable
    public CoreCharacterData getCharacter(int slot) {
        if (coreCharacterDataMap.get(slot) != null) {
            return coreCharacterDataMap.get(slot);
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

}
