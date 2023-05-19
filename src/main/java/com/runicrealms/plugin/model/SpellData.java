package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpellData implements SessionDataRedis {
    public static final List<String> FIELDS = new ArrayList<>() {{
        add(SpellField.HOT_BAR_ONE.getField());
        add(SpellField.LEFT_CLICK.getField());
        add(SpellField.RIGHT_CLICK.getField());
        add(SpellField.SWAP_HANDS.getField());
    }};

    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Sacred Spring";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Sprint";
    public static final String DEFAULT_WARRIOR = "Slam";
    private String spellHotbarOne = "";
    private String spellLeftClick = "";
    private String spellRightClick = "";
    private String spellSwapHands = "";

    @SuppressWarnings("unused")
    public SpellData() {
        // Default constructor for Spring
    }

    /**
     * Used for creating spell data from scratch
     *
     * @param playerClass of the player's character
     */
    public SpellData(String playerClass) {
        this.spellHotbarOne = determineDefaultSpell(playerClass);
    }

    /**
     * Constructs a spell wrapper for the given player with specified spells, which can be left blank.
     *
     * @param spellHotbarOne  spell assigned to hotbar 1
     * @param spellLeftClick  spell assigned to left-click
     * @param spellRightClick spell assigned to right-click
     * @param spellSwapHands  spell assigned to swap hands
     */
    public SpellData(String spellHotbarOne, String spellLeftClick,
                     String spellRightClick, String spellSwapHands) {
        this.spellHotbarOne = spellHotbarOne;
        this.spellLeftClick = spellLeftClick;
        this.spellRightClick = spellRightClick;
        this.spellSwapHands = spellSwapHands;
    }

    /**
     * Build the player's spell data from redis
     *
     * @param slot  the chosen character slot from the select screen
     * @param jedis the jedis resource
     */
    public SpellData(UUID uuid, int slot, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        Map<String, String> fieldsMap = jedis.hgetAll(database + ":" + getJedisKey(uuid, slot));
        this.spellHotbarOne = fieldsMap.get(SpellField.HOT_BAR_ONE.getField());
        this.spellLeftClick = fieldsMap.get(SpellField.LEFT_CLICK.getField());
        this.spellRightClick = fieldsMap.get(SpellField.RIGHT_CLICK.getField());
        this.spellSwapHands = fieldsMap.get(SpellField.SWAP_HANDS.getField());
    }

    /**
     * Determines the default starter spell for each class
     *
     * @param playerClass of the player's character
     * @return a string corresponding to the spell name of the starter spell
     */
    public static String determineDefaultSpell(String playerClass) {
        return switch (playerClass) {
            case "Archer" -> DEFAULT_ARCHER;
            case "Cleric" -> DEFAULT_CLERIC;
            case "Mage" -> DEFAULT_MAGE;
            case "Rogue" -> DEFAULT_ROGUE;
            case "Warrior" -> DEFAULT_WARRIOR;
            default -> "";
        };
    }

    /**
     * Helper method to look up the jedis key corresponding to this section of the data
     *
     * @param uuid of the player
     * @param slot of the character
     * @return a string key which is used to identify all relevant spell data
     */
    public static String getJedisKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot + ":" + SkillTreeData.PATH_LOCATION + ":" + SkillTreeData.SPELLS_LOCATION;
    }

    @Override
    public Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<String> fields = new ArrayList<>(getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        List<String> values = jedis.hmget(database + ":" + uuid + ":character:" + slot[0], fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        return fieldsMap;
    }

    @Override
    public List<String> getFields() {
        return FIELDS;
    }

    @Override
    public Map<String, String> toMap(UUID uuid, int... slot) {
        return new HashMap<>() {{
            put(SpellField.HOT_BAR_ONE.getField(), spellHotbarOne);
            put(SpellField.LEFT_CLICK.getField(), spellLeftClick);
            put(SpellField.RIGHT_CLICK.getField(), spellRightClick);
            put(SpellField.SWAP_HANDS.getField(), spellSwapHands);
        }};
    }

    /**
     * Adds the object into session storage in jedis
     *
     * @param jedis the jedis resource
     * @param slot  the character slot
     */
    @Override
    public void writeToJedis(UUID uuid, Jedis jedis, int... slot) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", uuid.toString());
        // Ensure the system knows that there is data in redis
        jedis.sadd(database + ":" + uuid + ":spellData", String.valueOf(slot[0]));
        jedis.expire(database + ":" + uuid + ":spellData", RunicCore.getRedisAPI().getExpireTime());
        String key = getJedisKey(uuid, slot[0]);
        jedis.hmset(database + ":" + key, this.toMap(uuid));
        jedis.expire(database + ":" + key, RunicCore.getRedisAPI().getExpireTime());
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

    /**
     * Reset the spells to their defaults
     *
     * @param playerClass of the character from the core player data
     */
    public void resetSpells(String playerClass) {
        this.spellHotbarOne = determineDefaultSpell(playerClass);
        this.spellLeftClick = "";
        this.spellRightClick = "";
        this.spellSwapHands = "";
    }

}
