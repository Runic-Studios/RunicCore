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
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.redis.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Caches three skill trees per-player, one for each subclass.
 */
public class SkillTreeManager implements Listener {
    private final Map<UUID, Set<String>> playerPassiveMap = new HashMap<>();
    private final Map<String, SkillTreeData> playerSkillTreeMap = new HashMap<>(); // memoization, keyed by uuid + ":" + position
    private final Map<UUID, PlayerSpellData> playerSpellMap = new HashMap<>(); // memoization
    private final Map<UUID, Integer> playerSpentPointsMap = new HashMap<>(); // memoization

    public SkillTreeManager() {
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Saves player skill tree info whenever the player cache is saved.
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        UUID uuid = event.getUuid();
        int slot = event.getSlot();
        Jedis jedis = event.getJedis();
        PlayerMongoData playerMongoData = event.getMongoData();
        PlayerSpellData playerSpellData = RunicCore.getSkillTreeManager().loadPlayerSpellData(uuid, slot, jedis);
        SkillTreeData first = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST, jedis);
        SkillTreeData second = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND, jedis);
        SkillTreeData third = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD, jedis);
        List<SkillTreeData> skillTreeDataList = new ArrayList<SkillTreeData>() {{
            add(first);
            add(second);
            add(third);
        }};
        skillTreeDataList.forEach(skillTreeData -> skillTreeData.writeToMongo(playerMongoData, slot));
        saveSpentPoints(uuid, event.getMongoDataSection());
        playerSpellData.writeToMongo(playerMongoData, slot);
    }

    @EventHandler(priority = EventPriority.HIGH) // loads last, but BEFORE StatManager
    public void onLoad(CharacterSelectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getCharacterData().getBaseCharacterInfo().getSlot();
        Jedis jedis = event.getJedis();
        /*
        Ensures spell-related data is properly memoized
         */
        // todo: make sure updates expiry on login
        this.playerPassiveMap.put(uuid, new HashSet<>()); // setup for passive map
        this.playerSpellMap.put(uuid, loadPlayerSpellData(uuid, slot, jedis)); // memoize spell data
        /*
        Ensure skill tree data is in redis
         */
        // todo: make sure every login updates expiry (for points and spells, too)
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.FIRST.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST, jedis)
                );
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.SECOND.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND, jedis)
                );
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.THIRD.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD, jedis)
                );
        // todo: make sure updates expiry on login
        int points = loadSpentPointsData(uuid, slot, jedis);
        if (points > PlayerLevelUtil.getMaxLevel() - (SkillTreeData.FIRST_POINT_LEVEL - 1))
            points = PlayerLevelUtil.getMaxLevel() - (SkillTreeData.FIRST_POINT_LEVEL - 1);
        RunicCoreAPI.setRedisValue(player, SkillTreeField.SPENT_POINTS.getField(), String.valueOf(points), jedis);
        this.playerSpentPointsMap.put(uuid, points); // memoize spent points for faster lookup
    }

    /**
     * Checks redis to see if the currently selected character's spent points (skill tree) data is cached
     * And if it is, returns the int
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return an int if found in redis
     */
    public int checkRedisForSpentPoints(UUID uuid, Integer slot, Jedis jedis) {
        jedis.auth(RedisManager.REDIS_PASSWORD);
        String key = uuid + ":character:" + slot;
        if (jedis.exists(key)) {
            String spentPoints = jedis.hmget(key, SkillTreeField.SPENT_POINTS.getField()).get(0);
            if (spentPoints != null && !spentPoints.equals(""))
                return Integer.parseInt(jedis.hmget(key, SkillTreeField.SPENT_POINTS.getField()).get(0));
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis spent points data not found");
        return -1;
    }

    /**
     * Determines how many points the player has spent on their skill tree.
     * Tries to build it from session storage (Redis) first, then falls back to Mongo
     *
     * @param uuid  of player who is attempting to load their data
     * @param slot  the slot of the character
     * @param jedis the jedis resource
     */
    public int loadSpentPointsData(UUID uuid, Integer slot, Jedis jedis) {
        // Step 1: check if spent points data is cached in redis
        int spentPoints = checkRedisForSpentPoints(uuid, slot, jedis);
        if (spentPoints != -1) return spentPoints;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        if (character.has(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.POINTS_LOCATION)) {
            int points = character.get(SkillTreeData.PATH_LOCATION + "." + SkillTreeData.POINTS_LOCATION, Integer.class);
            RunicCoreAPI.setRedisValue(Bukkit.getPlayer(uuid), SkillTreeField.SPENT_POINTS.getField(), String.valueOf(points), jedis);
            return points;
        } else {
            RunicCoreAPI.setRedisValue(Bukkit.getPlayer(uuid), SkillTreeField.SPENT_POINTS.getField(), "0", jedis);
            return 0;
        }
    }

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return a SpellData object if it is found in redis
     */
    public PlayerSpellData checkRedisForSpellData(UUID uuid, Integer slot, Jedis jedis) {
        if (jedis.exists(PlayerSpellData.getJedisKey(uuid, slot))) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "redis spell data found, building spell data from redis");
            return new PlayerSpellData(uuid, slot, jedis);
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
    public PlayerSpellData loadPlayerSpellData(UUID uuid, Integer slot, Jedis jedis) {
        // Step 1: check if spell data is memoized
        PlayerSpellData playerSpellData = this.getPlayerSpellMap().get(uuid);
        if (playerSpellData != null) return playerSpellData;
        // Step 2: check if spell data is cached in redis
        playerSpellData = checkRedisForSpellData(uuid, slot, jedis);
        if (playerSpellData != null) return playerSpellData;
        // Step 3: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new PlayerSpellData
                (
                        uuid,
                        character,
                        jedis
                );
    }

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid              of player to check
     * @param slot              of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @param jedis             the jedis resource
     * @return a SpellData object if it is found in redis
     */
    public SkillTreeData checkRedisForSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition, Jedis jedis) {
        if (jedis.exists(SkillTreeData.getJedisKey(uuid, slot, skillTreePosition))) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "redis skill tree data found, building skill tree data from redis");
            return new SkillTreeData(uuid, slot, skillTreePosition, jedis);
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
    public SkillTreeData loadSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition, Jedis jedis) {
        // Step 1: check if skill tree data is memoized
        SkillTreeData skillTreeData = this.getPlayerSkillTreeMap().get(uuid + ":" + skillTreePosition.getValue());
        if (skillTreeData != null) return skillTreeData;
        // Step 2: check if skill tree data is cached in redis
        skillTreeData = checkRedisForSkillTreeData(uuid, slot, skillTreePosition, jedis);
        if (skillTreeData != null) return skillTreeData;
        // Step 3: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new SkillTreeData(uuid, slot, skillTreePosition, character, jedis);
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

    public Map<UUID, Set<String>> getPlayerPassiveMap() {
        return this.playerPassiveMap;
    }

    public Map<String, SkillTreeData> getPlayerSkillTreeMap() {
        return this.playerSkillTreeMap;
    }

    public Map<UUID, PlayerSpellData> getPlayerSpellMap() {
        return this.playerSpellMap;
    }

    public Map<UUID, Integer> getPlayerSpentPointsMap() {
        return this.playerSpentPointsMap;
    }
}
