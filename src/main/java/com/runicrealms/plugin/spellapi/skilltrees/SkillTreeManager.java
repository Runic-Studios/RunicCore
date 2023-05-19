package com.runicrealms.plugin.spellapi.skilltrees;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SkillTreeAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caches three skill trees per-player, one for each subclass.
 */
public class SkillTreeManager implements Listener, SkillTreeAPI {
    private final Map<UUID, Set<String>> playerPassiveMap = new HashMap<>();

    public SkillTreeManager() {
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
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
        Set<String> redisSkillTreeSet = RunicCore.getRedisAPI().getRedisDataSet(uuid, "skillTreeData", jedis);
        boolean dataInRedis = RunicCore.getRedisAPI().determineIfDataInRedis(redisSkillTreeSet, slot);
        if (dataInRedis) {
//            Bukkit.getLogger().info("Redis skill tree data found, building skill tree data from redis");
            return new SkillTreeData(uuid, slot, skillTreePosition, jedis);
        }
        return null;
    }

    @Override
    public SpellData checkRedisForSpellData(UUID uuid, Integer slot, Jedis jedis) {
        Set<String> redisSpellSet = RunicCore.getRedisAPI().getRedisDataSet(uuid, "spellData", jedis);
        boolean dataInRedis = RunicCore.getRedisAPI().determineIfDataInRedis(redisSpellSet, slot);
        if (dataInRedis) {
//            Bukkit.getLogger().info("redis spell data found, building spell data from redis");
            return new SpellData(uuid, slot, jedis);
        }
        return null;
    }

    @Override
    public int getAvailableSkillPoints(UUID uuid, int slot) {
        return SkillTreeData.getAvailablePoints(uuid, slot);
    }

    @Override
    public Set<String> getPassives(UUID uuid) {
        return playerPassiveMap.get(uuid);
    }

    @Override
    public SpellData getPlayerSpellData(UUID uuid, int slot) {
        return RunicCore.getDataAPI().getCorePlayerData(uuid).getSpellDataMap().get(slot);
    }

    @Override
    public int getSpentPoints(UUID uuid, int slot) {
        int spentPoints = 0;
        SkillTreeData first = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeAPI().loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
        for (Perk perk : first.getPerks()) {
            spentPoints += perk.getCurrentlyAllocatedPoints();
        }
        for (Perk perk : second.getPerks()) {
            spentPoints += perk.getCurrentlyAllocatedPoints();
        }
        for (Perk perk : third.getPerks()) {
            spentPoints += perk.getCurrentlyAllocatedPoints();
        }
        return spentPoints;
    }

    @Override
    public boolean hasPassiveFromSkillTree(UUID uuid, String passive) {
        if (playerPassiveMap.get(uuid) == null) {
            return false; // player not setup yet from async select event
        }
        return playerPassiveMap.get(uuid).contains(passive);
    }

    /**
     * Creates a SpellData object. Tries to build it from memory first, then falls back to redis/mongo
     *
     * @param uuid     of player who is attempting to load their data
     * @param slot     the slot of the character
     * @param position which position is the skill tree? first, second, etc.
     * @return a SkillTreeData object
     */
    @Override
    public SkillTreeData loadSkillTreeData(UUID uuid, int slot, SkillTreePosition position) {
        return RunicCore.getDataAPI().getCorePlayerData(uuid).getSkillTreeData(slot, position);
    }

    @Override
    public SpellData loadSpellData(UUID uuid, Integer slot, Jedis jedis, String playerClass) {
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        // Step 1: Check Redis
        SpellData spellData = checkRedisForSpellData(uuid, slot, jedis);
        if (spellData != null) {
            corePlayerData.getSpellDataMap().put(slot, spellData);
            Bukkit.getLogger().info("LOADING SPELL DATA FROM REDIS");
            return spellData;
        }
        // Step 2: Check the Mongo database
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("spellDataMap");
        SpellData spellDataMongo = RunicCore.getDataAPI().getMongoTemplate().findOne(query, SpellData.class);
        if (spellDataMongo != null) {
            corePlayerData.getSpellDataMap().put(slot, spellDataMongo);
            spellDataMongo.writeToJedis(uuid, jedis);
            return spellDataMongo;
        }
        // Step 3: Create new data and add to in-memory object
        SpellData newData = new SpellData(corePlayerData.getCharacter(slot).getClassType().getName());
        corePlayerData.getSpellDataMap().put(slot, newData);
        return newData;
    }

    @Override
    public SpellData loadSpellDataFromMemory(UUID uuid, Integer slot, String playerClass) {
        return this.getPlayerSpellData(uuid, slot);
    }

    @Override
    public SkillTreeGUI skillTreeGUI(Player player, SkillTreePosition position) {
        UUID uuid = player.getUniqueId();
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(uuid);
        SkillTreeData skillTreeData = loadSkillTreeData(uuid, slot, position);
        return new SkillTreeGUI(player, skillTreeData);
    }

    /**
     * Loads all skill trees for the specified player
     *
     * @return a list of SkillTrees
     */
    private List<SkillTreeData> loadAllSkillTrees(UUID uuid, int slot, Jedis jedis, String className) {
        List<SkillTreePosition> positions = Arrays.asList(SkillTreePosition.FIRST, SkillTreePosition.SECOND, SkillTreePosition.THIRD);
        return positions.stream()
                .map(position -> loadSkillTreeData(uuid, slot, position, jedis, className))
                .collect(Collectors.toList());
    }

    /**
     * Creates a SpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid     of player who is attempting to load their data
     * @param slot     the slot of the character
     * @param position which position is the skill tree? first, second, etc.
     * @param jedis    the jedis resource
     * @return a SkillTreeData object
     */
    public SkillTreeData loadSkillTreeData(UUID uuid, Integer slot, SkillTreePosition position, Jedis jedis, String playerClass) {
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        // Step 1: Check Redis
        SkillTreeData skillTreeData = checkRedisForSkillTreeData(uuid, slot, position, jedis);
        if (skillTreeData != null) {
            corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
            corePlayerData.getSkillTreeDataMap().get(slot).put(position, skillTreeData);
            Bukkit.getLogger().info("LOADING SKILL TREE DATA FROM REDIS");
            return skillTreeData;
        }
        // Step 2: Check the Mongo database
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("skillTreeDataMap");
        SkillTreeData skillTreeMongo = RunicCore.getDataAPI().getMongoTemplate().findOne(query, SkillTreeData.class);
        if (skillTreeMongo != null) {
            corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
            corePlayerData.getSkillTreeDataMap().get(slot).put(position, skillTreeMongo);
            skillTreeMongo.writeToJedis(uuid, jedis);
            return skillTreeMongo;
        }
        // Step 3: Create new data and add to in-memory object
        SkillTreeData newData = new SkillTreeData(uuid, position, playerClass);
        corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
        corePlayerData.getSkillTreeDataMap().get(slot).put(position, newData);
        return newData;

    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            saveSkillTreesToJedis
                    (
                            event.getPlayer().getUniqueId(),
                            event.getSlot(),
                            jedis,
                            event.getCoreCharacterData().getClassType().getName()
                    );
        }
        this.playerPassiveMap.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Load all spell and skill tree data for the character
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCharacterSelect(CharacterSelectEvent event) {
        // For benchmarking
        long startTime = System.nanoTime();
        event.getPluginsToLoadData().add("core.spells");
        event.getPluginsToLoadData().add("core.skillTrees");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();

        // Ensures spell-related data is properly memoized
        this.playerPassiveMap.put(uuid, new HashSet<>()); // setup for passive map
        CorePlayerData corePlayerData = event.getCorePlayerData();
        String className = event.getCorePlayerData().getCharacter(slot).getClassType().getName();

        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            TaskChain<?> spellSetupChain = RunicCore.newChain();
            spellSetupChain
                    .asyncFirst(() -> loadSpellData(uuid, slot, jedis, className))
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load spells on join!")
                    .syncLast(spellData -> {
                        corePlayerData.getSpellDataMap().put(slot, spellData); // Memoize spell data
                        event.getPluginsToLoadData().remove("core.spells");
                        // Calculate elapsed time
                        long endTime = System.nanoTime();
                        long elapsedTime = endTime - startTime;
                        // Log elapsed time in milliseconds
                        Bukkit.getLogger().info("RunicCore|spells took: " + elapsedTime / 1_000_000 + "ms to load");
                    })
                    .execute();
            // SkillTree setup (load from redis and memoize)
            TaskChain<?> skillTreeSetupChain = RunicCore.newChain();
            skillTreeSetupChain
                    .asyncFirst(() -> loadAllSkillTrees(uuid, slot, jedis, className))
                    .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load spells on join!")
                    .syncLast(skillTreeDataList -> {
                        corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
//                        Bukkit.getLogger().log(Level.INFO, "adding skill trees to in-memory map");
                        skillTreeDataList.forEach(skillTreeData -> {
                            corePlayerData.getSkillTreeDataMap().get(slot).put(skillTreeData.getPosition(), skillTreeData);
                            // Populate passives to in-memory map from the skill tree
                            skillTreeData.addPassivesToMap(uuid);
                        });
                        event.getPluginsToLoadData().remove("core.skillTrees");
                        // Calculate elapsed time
                        long endTime = System.nanoTime();
                        long elapsedTime = endTime - startTime;
                        // Log elapsed time in milliseconds
                        Bukkit.getLogger().info("RunicCore|skillTrees took: " + elapsedTime / 1_000_000 + "ms to load");
                    })
                    .execute();
        }

    }

    /**
     * Saves all data relevant to skill trees to jedis, including spell data and allocated points
     *
     * @param uuid  of the player to save
     * @param slot  of the character
     * @param jedis the jedis resource
     */
    private void saveSkillTreesToJedis(UUID uuid, int slot, Jedis jedis, String playerClass) {
        // Inform the server that this player should be saved to mongo on next task (jedis data is refreshed)
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        jedis.sadd(database + ":" + "markedForSave:core", uuid.toString());
        SpellData playerSpellData = loadSpellDataFromMemory(uuid, slot, playerClass);
        SkillTreeData first = loadSkillTreeData(uuid, slot, SkillTreePosition.FIRST);
        SkillTreeData second = loadSkillTreeData(uuid, slot, SkillTreePosition.SECOND);
        SkillTreeData third = loadSkillTreeData(uuid, slot, SkillTreePosition.THIRD);
        List<SkillTreeData> skillTreeDataList = new ArrayList<>() {{
            add(first);
            add(second);
            add(third);
        }};
        skillTreeDataList.forEach(skillTreeData -> skillTreeData.writeToJedis(uuid, jedis, slot));
        playerSpellData.writeToJedis(uuid, jedis, slot);
    }

}
