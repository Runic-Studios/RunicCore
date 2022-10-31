package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
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

    public SkillTreeManager() {
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Saves player skill tree info when the server is shut down
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        for (UUID uuid : event.getPlayersToSave().keySet()) {
            for (int characterSlot : event.getPlayersToSave().get(uuid).getCharactersToSave()) {
                PlayerMongoData playerMongoData = event.getPlayersToSave().get(uuid).getPlayerMongoData();
                PlayerMongoDataSection character = playerMongoData.getCharacter(characterSlot);
                saveSpellsAndSkillTreesToMongo(uuid, characterSlot, event.getJedis(), playerMongoData, character);
            }
        }
    }

    /**
     * Saves all spell-related and skill tree data to mongo on a server shutdown
     *
     * @param uuid            of the player
     * @param slot            of the character
     * @param jedis           the jedis resource
     * @param playerMongoData the player's data in mongo
     * @param character       the character's mongo data section
     */
    private void saveSpellsAndSkillTreesToMongo(UUID uuid, int slot, Jedis jedis,
                                                PlayerMongoData playerMongoData, PlayerMongoDataSection character) {
        character.remove(SkillTreeData.PATH_LOCATION); // removes ALL THREE SkillTree data sections AND spent points
        PlayerSpellData playerSpellData = RunicCore.getSkillTreeManager().loadPlayerSpellData(uuid, slot);
        SkillTreeData first = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
        first.writeToMongo(playerMongoData, slot);
        second.writeToMongo(playerMongoData, slot);
        third.writeToMongo(playerMongoData, slot);
        playerSpellData.writeToMongo(playerMongoData, slot);
    }

    @EventHandler(priority = EventPriority.HIGH) // loads last, but BEFORE StatManager
    public void onCharacterSelect(CharacterSelectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getCharacterData().getBaseCharacterInfo().getSlot();
        /*
        Ensures spell-related data is properly memoized
         */
        this.playerPassiveMap.put(uuid, new HashSet<>()); // setup for passive map
        this.playerSpellMap.put(uuid, loadPlayerSpellData(uuid, slot)); // memoize spell data
        /*
        Ensure skill tree data is in redis
         */
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.FIRST.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST)
                );
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.SECOND.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND)
                );
        this.playerSkillTreeMap.put
                (
                        uuid + ":" + SkillTreePosition.THIRD.getValue(),
                        loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD)
                );
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        saveSkillTreesToJedis(event.getPlayer().getUniqueId(), event.getSlot(), event.getJedis());
        removeDataFromMemory(event.getPlayer().getUniqueId());
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
            // Bukkit.broadcastMessage(ChatColor.GREEN + "redis spell data found, building spell data from redis");
            return new PlayerSpellData(uuid, slot, jedis);
        }
        // Bukkit.broadcastMessage(ChatColor.RED + "redis spell data not found");
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
        // Step 1: check if spell data is memoized
        PlayerSpellData playerSpellData = this.getPlayerSpellMap().get(uuid);
        if (playerSpellData != null) return playerSpellData;
        // Step 2: check if spell data is cached in redis
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            return loadPlayerSpellData(uuid, slot, jedis);
        }
    }

    /**
     * Creates a PlayerSpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    public PlayerSpellData loadPlayerSpellData(UUID uuid, Integer slot, Jedis jedis) {
        // Step 1: check if spell data is cached in redis
        PlayerSpellData playerSpellData = checkRedisForSpellData(uuid, slot, jedis);
        if (playerSpellData != null) return playerSpellData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new PlayerSpellData
                (
                        uuid,
                        slot,
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
            // Bukkit.broadcastMessage(ChatColor.GREEN + "redis skill tree data found, building skill tree data from redis");
            return new SkillTreeData(uuid, slot, skillTreePosition, jedis);
        }
        // Bukkit.broadcastMessage(ChatColor.RED + "redis skill tree data not found");
        return null;
    }

    /**
     * Creates a PlayerSpellData object. Tries to build it from memory first, then falls back to redis/mongo
     *
     * @param uuid              of player who is attempting to load their data
     * @param slot              the slot of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @return a SkillTreeData object
     */
    public SkillTreeData loadSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition) {
        // Step 1: check if skill tree data is memoized
        SkillTreeData skillTreeData = this.getPlayerSkillTreeMap().get(uuid + ":" + skillTreePosition.getValue());
        if (skillTreeData != null) return skillTreeData;
        // Step 2: check if skill tree data is cached in redis
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            return loadSkillTreeData(uuid, slot, skillTreePosition, jedis);
        }
    }

    /**
     * Creates a PlayerSpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid              of player who is attempting to load their data
     * @param slot              the slot of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @param jedis             the jedis resource
     * @return a SkillTreeData object
     */
    public SkillTreeData loadSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition, Jedis jedis) {
        // Step 1: check if skill tree data is cached in redis
        SkillTreeData skillTreeData = checkRedisForSkillTreeData(uuid, slot, skillTreePosition, jedis);
        if (skillTreeData != null) return skillTreeData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        return new SkillTreeData(uuid, slot, skillTreePosition, character, jedis);
    }

    /**
     * Removes any memoized data for the given player related to skill trees
     *
     * @param uuid of the player to remove
     */
    private void removeDataFromMemory(UUID uuid) {
        this.playerSkillTreeMap.remove(uuid + ":" + SkillTreePosition.FIRST.getValue());
        this.playerSkillTreeMap.remove(uuid + ":" + SkillTreePosition.SECOND.getValue());
        this.playerSkillTreeMap.remove(uuid + ":" + SkillTreePosition.THIRD.getValue());
        this.playerSpellMap.remove(uuid);
        this.playerPassiveMap.remove(uuid);
    }

    /**
     * Saves all data relevant to skill trees to jedis, including spell data and allocated points
     *
     * @param uuid  of the player to save
     * @param slot  of the character
     * @param jedis the jedis resource
     */
    private void saveSkillTreesToJedis(UUID uuid, int slot, Jedis jedis) {
        PlayerSpellData playerSpellData = RunicCore.getSkillTreeManager().loadPlayerSpellData(uuid, slot);
        SkillTreeData first = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeManager().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
        List<SkillTreeData> skillTreeDataList = new ArrayList<SkillTreeData>() {{
            add(first);
            add(second);
            add(third);
        }};
        skillTreeDataList.forEach(skillTreeData -> skillTreeData.writeToJedis(jedis, slot));
        playerSpellData.writeToJedis(jedis, slot);
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
}
