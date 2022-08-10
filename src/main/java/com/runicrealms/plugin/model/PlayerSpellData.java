package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.model.cache.SpellWrapper;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.redis.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapper to store the assignment of spells to each spell slot
 */
public class PlayerSpellData implements SessionData {
    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Holy Water";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Sprint";
    public static final String DEFAULT_WARRIOR = "Slam";

    private String spellHotbarOne;
    private String spellLeftClick;
    private String spellRightClick;
    private String spellSwapHands;
    private final UUID uuid;

    /**
     * Constructs a spell wrapper for the given player with specified spells, which can be left blank.
     *
     * @param uuid            to generate wrapper for
     * @param spellHotbarOne  spell assigned to hotbar 1
     * @param spellLeftClick  spell assigned to left-click
     * @param spellRightClick spell assigned to right-click
     * @param spellSwapHands  spell assigned to swap hands
     */
    public PlayerSpellData(UUID uuid, String spellHotbarOne, String spellLeftClick,
                           String spellRightClick, String spellSwapHands) {
        this.uuid = uuid;
        this.spellHotbarOne = spellHotbarOne;
        this.spellLeftClick = spellLeftClick;
        this.spellRightClick = spellRightClick;
        this.spellSwapHands = spellSwapHands;
        updateInMemorySpellMap();
    }

    /**
     * Constructs a player spell wrapper from DB
     *
     * @param uuid   of player to generate wrapper for
     * @param slot   of the character
     * @param spells spells section of DB for character
     */
    public PlayerSpellData(UUID uuid, int slot, PlayerMongoDataSection spells) {
        this.uuid = uuid;
        this.spellHotbarOne = spells.get(SpellField.HOT_BAR_ONE.getField(), String.class);
        this.spellLeftClick = spells.get(SpellField.LEFT_CLICK.getField(), String.class);
        this.spellRightClick = spells.get(SpellField.RIGHT_CLICK.getField(), String.class);
        this.spellSwapHands = spells.get(SpellField.SWAP_HANDS.getField(), String.class);
        // call each skill tree and populate
        RunicCoreAPI.getSkillTree(uuid, slot, SkillTreePosition.FIRST).addPassivesToMap();
        RunicCoreAPI.getSkillTree(uuid, slot, SkillTreePosition.SECOND).addPassivesToMap();
        RunicCoreAPI.getSkillTree(uuid, slot, SkillTreePosition.THIRD).addPassivesToMap();
        updateInMemorySpellMap();
        writeSpellDataToJedis(RunicCore.getRedisManager().getJedisPool());
    }

    /**
     * Build the player's spell data from redis
     *
     * @param uuid  of the player that selected the character profile
     * @param slot  the chosen character slot from the select screen
     * @param jedis the jedis resource
     */
    public PlayerSpellData(UUID uuid, int slot, Jedis jedis) {
        Map<String, String> fieldsMap = jedis.hgetAll(getJedisKey(uuid, slot));
        this.uuid = uuid;
        this.spellHotbarOne = fieldsMap.get(SpellField.HOT_BAR_ONE.getField());
        this.spellLeftClick = fieldsMap.get(SpellField.LEFT_CLICK.getField());
        this.spellRightClick = fieldsMap.get(SpellField.RIGHT_CLICK.getField());
        this.spellSwapHands = fieldsMap.get(SpellField.SWAP_HANDS.getField());
        updateInMemorySpellMap();
    }

    /**
     * Adds the object into session storage in redis
     *
     * @param jedisPool the jedis pool resource from core
     */
    public void writeSpellDataToJedis(JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(RedisManager.REDIS_PASSWORD);
            String key = getJedisKey(uuid, RunicCoreAPI.getCharacterSlot(uuid));
            jedis.hmset(key, this.toMap());
            jedis.expire(key, RedisUtil.EXPIRE_TIME);
        }
    }

    private void updateInMemorySpellMap() {
        SpellWrapper spellWrapper = RunicCore.getSkillTreeManager().getPlayerSpellMap().get(uuid);
        spellWrapper.setSpellHotbarOne(this.spellHotbarOne);
        spellWrapper.setSpellLeftClick(this.spellLeftClick);
        spellWrapper.setSpellRightClick(this.spellRightClick);
        spellWrapper.setSpellSwapHands(this.spellSwapHands);
    }

    public static String determineDefaultSpell(UUID uuid) {
        int slot = RunicCoreAPI.getCharacterSlot(uuid);
        switch (RunicCoreAPI.getRedisCharacterValue(uuid, CharacterField.CLASS_TYPE.getField(), slot)) {
            case "Archer":
                return DEFAULT_ARCHER;
            case "Cleric":
                return DEFAULT_CLERIC;
            case "Mage":
                return DEFAULT_MAGE;
            case "Rogue":
                return DEFAULT_ROGUE;
            case "Warrior":
                return DEFAULT_WARRIOR;
            default:
                return "";
        }
    }

    /**
     * Helper method to
     *
     * @param uuid
     * @param slot
     * @return
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":" + SkillTreeData.PATH_LOCATION + ":" + SkillTreeData.SPELLS_LOCATION;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSpellHotbarOne() {
        return spellHotbarOne;
    }

    public void setSpellHotbarOne(String spellHotbarOne) {
        this.spellHotbarOne = spellHotbarOne;
    }

    public String getSpellLeftClick() {
        return spellLeftClick;
    }

    public void setSpellLeftClick(String spellLeftClick) {
        this.spellLeftClick = spellLeftClick;
    }

    public String getSpellRightClick() {
        return spellRightClick;
    }

    public void setSpellRightClick(String spellRightClick) {
        this.spellRightClick = spellRightClick;
    }

    public String getSpellSwapHands() {
        return spellSwapHands;
    }

    public void setSpellSwapHands(String spellSwapHands) {
        this.spellSwapHands = spellSwapHands;
    }

    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put(SpellField.HOT_BAR_ONE.getField(), spellHotbarOne);
            put(SpellField.LEFT_CLICK.getField(), spellLeftClick);
            put(SpellField.RIGHT_CLICK.getField(), spellRightClick);
            put(SpellField.SWAP_HANDS.getField(), spellSwapHands);
        }};
    }

    @Override
    public void writeToMongo(PlayerMongoData playerMongoData, int... slot) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot[0]);
        PlayerMongoDataSection spells = (PlayerMongoDataSection) character.getSection(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.SPELLS_LOCATION);
        spells.set(SpellField.HOT_BAR_ONE.getField(), this.spellHotbarOne);
        spells.set(SpellField.LEFT_CLICK.getField(), this.spellLeftClick);
        spells.set(SpellField.RIGHT_CLICK.getField(), this.spellRightClick);
        spells.set(SpellField.SWAP_HANDS.getField(), this.spellSwapHands);
    }
}
