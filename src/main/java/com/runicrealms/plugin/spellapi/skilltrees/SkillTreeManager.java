package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreeField;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.model.cache.SpellWrapper;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Caches three skill trees per-player, one for each subclass.
 */
public class SkillTreeManager implements Listener {

    private final Map<UUID, Set<String>> playerPassiveMap = new HashMap<>();
    private final Map<UUID, SpellWrapper> playerSpellMap = new HashMap<>();

    public SkillTreeManager() {
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Saves player skill tree info whenever the player cache is saved.
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent e) {
        UUID uuid = e.getUuid();
        int slot = e.getSlot();
        PlayerMongoData playerMongoData = e.getMongoData();
        PlayerSpellData playerSpellData = RunicCore.getSkillTreeManager().loadPlayerSpellData(uuid, slot);
        SkillTreeData first = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
        first.writeToMongo(playerMongoData, slot);
        second.writeToMongo(playerMongoData, slot);
        third.writeToMongo(playerMongoData, slot);
        if (getSpentPoints(uuid, slot) != 0) // todo: remove if check?
            saveSpentPoints(uuid, e.getMongoDataSection());
        playerSpellData.writeToMongo(playerMongoData, slot);
    }

    @EventHandler(priority = EventPriority.HIGH) // loads last, but BEFORE StatManager
    public void onLoad(CharacterSelectEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = e.getCharacterData().getBaseCharacterInfo().getSlot();
        Bukkit.broadcastMessage("the slot of the select event is: " + slot);
        /*
        Ensures spell-related data is properly memoized
         */
        this.playerPassiveMap.put(uuid, new HashSet<>()); // setup for passive map
        this.playerSpellMap.put(uuid, new SpellWrapper(uuid)); // default spell setup
        loadPlayerSpellData(uuid, slot);

        /*
        Ensure skill tree data is in redis
         */
        //todo: make sure every login updates expiry (for points and spells, too)
        loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);


        // todo: this should be a standard load method
        int points = 0;
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        if (character.has(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.POINTS_LOCATION))
            points = character.get(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.POINTS_LOCATION, Integer.class);
        if (points < 0) // insurance
            points = 0;
        if (points > PlayerLevelUtil.getMaxLevel() - (SkillTreeData.FIRST_POINT_LEVEL - 1))
            points = PlayerLevelUtil.getMaxLevel() - (SkillTreeData.FIRST_POINT_LEVEL - 1);
        RunicCoreAPI.setRedisValue(player, SkillTreeField.SPENT_POINTS.getField(), String.valueOf(points));
        // ------------------------------
    }

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return a SpellData object if it is found in redis
     */
    public PlayerSpellData checkRedisForSpellData(UUID uuid, Integer slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            if (jedis.exists(PlayerSpellData.getJedisKey(uuid, slot))) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "redis spell data found, building spell data from redis");
                return new PlayerSpellData(uuid, slot, jedis);
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis spell data not found");
        return null;
    }

    /**
     * Creates a PlayerSpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    public PlayerSpellData loadPlayerSpellData(UUID uuid, Integer slot) {
        // Step 1: check if spell data is cached in redis
        PlayerSpellData playerSpellData = checkRedisForSpellData(uuid, slot);
        if (playerSpellData != null) return playerSpellData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new PlayerSpellData
                (
                        uuid,
                        slot,
                        character
                );
    }

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid              of player to check
     * @param slot              of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @return a SpellData object if it is found in redis
     */
    public SkillTreeData checkRedisForSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            if (jedis.exists(SkillTreeData.getJedisKey(uuid, slot, skillTreePosition))) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "redis skill tree data found, building skill tree data from redis");
                return new SkillTreeData(uuid, slot, skillTreePosition, jedis);
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis skill tree data not found");
        return null;
    }

    /**
     * Creates a PlayerSpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid              of player who is attempting to load their data
     * @param slot              the slot of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @return a SkillTreeData object
     */
    public SkillTreeData loadSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition) {
        // Step 1: check if spell data is cached in redis
        SkillTreeData skillTreeData = checkRedisForSkillTreeData(uuid, slot, skillTreePosition);
        if (skillTreeData != null) return skillTreeData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new SkillTreeData(uuid, skillTreePosition, character);
    }

    /**
     * Saves the total spent points to DB
     *
     * @param uuid      of the player
     * @param character mongo data section from save event
     */
    private void saveSpentPoints(UUID uuid, PlayerMongoDataSection character) {
        character.set(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.POINTS_LOCATION, RunicCoreAPI.getSpentPoints(uuid));
    }

    /**
     * Returns the total allocated skill points of the given player character
     *
     * @param uuid of the player
     * @param slot of the character
     * @return the number of points they have spent
     */
    public int getSpentPoints(UUID uuid, int slot) {
        String spentPoints = RunicCoreAPI.getRedisCharacterValue(uuid, SkillTreeField.SPENT_POINTS.getField(), slot);
        if (!spentPoints.equals(""))
            return Integer.parseInt(RunicCoreAPI.getRedisCharacterValue(uuid, SkillTreeField.SPENT_POINTS.getField(), slot));
        else
            return 0;
    }

    public Map<UUID, Set<String>> getPlayerPassiveMap() {
        return this.playerPassiveMap;
    }

    public Map<UUID, SpellWrapper> getPlayerSpellMap() {
        return this.playerSpellMap;
    }
}
