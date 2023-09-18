package com.runicrealms.plugin.spellapi.skilltrees;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SkillTreeAPI;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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
        return RunicCore.getPlayerDataAPI().getCorePlayerData(uuid).getSpellDataMap().get(slot);
    }

    @Override
    public int getSpentPoints(UUID uuid, int slot) {
        int spentPoints = 0;
        Map<SkillTreePosition, SkillTreeData> skillTreeDataMap = RunicCore.getSkillTreeAPI().getSkillTreeDataMap(uuid, slot);
        SkillTreeData first = skillTreeDataMap.get(SkillTreePosition.FIRST);
        SkillTreeData second = skillTreeDataMap.get(SkillTreePosition.SECOND);
        SkillTreeData third = skillTreeDataMap.get(SkillTreePosition.THIRD);
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

    @Override
    public Map<SkillTreePosition, SkillTreeData> getSkillTreeDataMap(UUID uuid, int slot) {
        // Null if no CorePlayerData
        CorePlayerData corePlayerData = RunicCore.getPlayerDataAPI().getCorePlayerData(uuid);
        if (corePlayerData == null) {
            return null;
        }
        // Try to return in-memory cached skill tree data
        Map<Integer, Map<SkillTreePosition, SkillTreeData>> skillTreeDataMap = corePlayerData.getSkillTreeDataMap();
        if (skillTreeDataMap.get(slot) != null) {
            return skillTreeDataMap.get(slot);
        }
        return null;
    }

    @Override
    public SpellData loadSpellData(UUID uuid, Integer slot, CorePlayerData corePlayerData) {
        SpellData cached = corePlayerData.getSpellData(slot);
        if (cached != null) {
            return cached;
        }
        /*
        There is an architectural disconnect here, because SpellData is an embedded field of CorePlayerData, which
        IS cached in Redis, but SpellData doesn't interface with Redis anymore. So when CorePlayerData is loaded from
        Redis, it doesn't actually load the SpellData immediately, and we need another lookup.
         */
        // Step 2: Check the Mongo database
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("spellDataMap");
        CorePlayerData corePlayerDataMongo = RunicDatabase.getAPI().getDataAPI().getMongoTemplate().findOne(query, CorePlayerData.class);
        if (corePlayerDataMongo != null && corePlayerDataMongo.getSpellData(slot) != null) {
            corePlayerData.setSpellDataMap(corePlayerDataMongo.getSpellDataMap());
            return corePlayerData.getSpellData(slot);
        }
        // Step 3: Create new data and add to in-memory object
        SpellData newData = new SpellData(corePlayerData.getCharacter(slot).getClassType().getName());
        corePlayerData.getSpellDataMap().put(slot, newData);
        return newData;
    }

    @Override
    public SkillTreeGUI skillTreeGUI(Player player, SkillTreePosition position) {
        UUID uuid = player.getUniqueId();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        Map<SkillTreePosition, SkillTreeData> skillTreeDataMap = getSkillTreeDataMap(uuid, slot);
        return new SkillTreeGUI(player, skillTreeDataMap.get(position));
    }

    /**
     * Loads all skill trees for the specified player
     *
     * @return a list of SkillTrees
     */
    private List<SkillTreeData> loadAllSkillTrees(UUID uuid, int slot, String className, CorePlayerData corePlayerData) {
        List<SkillTreePosition> positions = Arrays.asList(SkillTreePosition.FIRST, SkillTreePosition.SECOND, SkillTreePosition.THIRD);
        return positions.stream()
                .map(position -> loadSkillTreeData(uuid, slot, position, className, corePlayerData))
                .collect(Collectors.toList());
    }

    @Override
    public SkillTreeData loadSkillTreeData(UUID uuid, int slot, SkillTreePosition position, String playerClass, CorePlayerData corePlayerData) {
        // Step 1: Check the Mongo database
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("skillTreeDataMap");
        CorePlayerData corePlayerDataMongo = RunicDatabase.getAPI().getDataAPI().getMongoTemplate().findOne(query, CorePlayerData.class);
        if (corePlayerDataMongo != null
                && corePlayerDataMongo.getSkillTreeDataMap() != null
                && corePlayerDataMongo.getSkillTreeDataMap().get(slot) != null) {
            corePlayerData.setSkillTreeDataMap(corePlayerDataMongo.getSkillTreeDataMap());
            corePlayerData.getSkillTreeData(slot, position).loadPerksFromDTOs(playerClass); // Loads our runtime perk list from stored field in mongo
            return corePlayerData.getSkillTreeDataMap().get(slot).get(position);
        }
        // Step 2: Create new data and add to in-memory object
        SkillTreeData newData = new SkillTreeData(position, playerClass);
        corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
        corePlayerData.getSkillTreeDataMap().get(slot).put(position, newData);
        return newData;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCharacterQuit(CharacterQuitEvent event) {
        this.playerPassiveMap.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Load all spell and skill tree data for the character
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCharacterSelect(CharacterSelectEvent event) {
        // For benchmarking
        long startTime = System.nanoTime();
        event.getPluginsToLoadData().add("core.skillTrees");
        event.getPluginsToLoadData().add("core.spells");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = event.getSlot();

        // Ensures spell-related data is properly memoized
        this.playerPassiveMap.put(uuid, new HashSet<>()); // setup for passive map
        CorePlayerData corePlayerData = (CorePlayerData) event.getSessionDataMongo();
        String className = corePlayerData.getCharacter(slot).getClassType().getName();


        // SkillTree setup (load from redis and memoize)
        TaskChain<?> skillTreeSetupChain = RunicCore.newChain();
        skillTreeSetupChain
                .asyncFirst(() -> loadAllSkillTrees(uuid, slot, className, (CorePlayerData) event.getSessionDataMongo()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load skill trees on join!")
                .syncLast(skillTreeDataList -> {
                    corePlayerData.getSkillTreeDataMap().computeIfAbsent(slot, k -> new HashMap<>());
                    skillTreeDataList.forEach(skillTreeData -> {
                        corePlayerData.getSkillTreeDataMap().get(slot).put(skillTreeData.getPosition(), skillTreeData);
                        // Populate passives to in-memory map from the skill tree
                        skillTreeData.addPassivesToMap(uuid);
                    });
                    event.getPluginsToLoadData().remove("core.skillTrees");
                })
                .asyncFirst(() -> loadSpellData(uuid, slot, (CorePlayerData) event.getSessionDataMongo()))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load spells on join!")
                .syncLast(spellData -> {
                    corePlayerData.getSpellDataMap().put(slot, spellData); // Memoize spell data
                    event.getPluginsToLoadData().remove("core.spells");
                })
                .execute(() -> {
                    // Calculate elapsed time
                    long endTime = System.nanoTime();
                    long elapsedTime = endTime - startTime;
                    // Log elapsed time in milliseconds
                    Bukkit.getLogger().info("RunicCore|skilltree and RunicCore|spells both took: " + elapsedTime / 1_000_000 + "ms to load");
                });
    }
}
