package com.runicrealms.plugin.model;

import co.aikar.taskchain.TaskChain;
import com.mongodb.bulk.BulkWriteResult;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.MongoTaskOperation;
import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the task that writes data from Redis --> MongoDB periodically
 *
 * @author Skyfallin
 */
public class MongoTask implements MongoTaskOperation {
    private static final int MONGO_TASK_TIME = 30; // seconds
    private final BukkitTask task;

    public MongoTask() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously
                (
                        RunicCore.getInstance(),
                        () -> saveAllToMongo(() -> {
                        }),
                        MONGO_TASK_TIME * 20L,
                        MONGO_TASK_TIME * 20L
                );
    }

    @Override
    public String getCollectionName() {
        return "core";
    }

    @Override
    public <T> Update getUpdate(T obj) {
        CorePlayerData corePlayerData = (CorePlayerData) obj;
        Update update = new Update();
        /*
        Only update keys in mongo with data in memory.
        If, for example, there's 5 characters with data in mongo but only 1 in redis,
        this only updates the character with new data.
         */
        update.set("lastLoginDate", corePlayerData.getLastLoginDate());
        /*
        Since we lazy-load character-specific data, we'll try to retrieve it for all slots which may exist in Redis.
        We do this for each character-specific field (skill trees, spells, etc.)
         */
        for (int slot = 1; slot < RunicCore.getDataAPI().getMaxCharacterSlot(); slot++) {
            CoreCharacterData characterData = corePlayerData.getCharacter(slot);
            if (characterData != null) {
//                Bukkit.getLogger().info("found character data in slot " + slot);
                update.set("coreCharacterDataMap." + slot, corePlayerData.getCoreCharacterDataMap().get(slot));
            }
        }
        // Update SkillTreeData
        for (int slot = 1; slot < RunicCore.getDataAPI().getMaxCharacterSlot(); slot++) {
            HashMap<SkillTreePosition, SkillTreeData> skillTreeData = corePlayerData.getSkillTreeData(slot);
            if (skillTreeData != null) {
//                Bukkit.getLogger().info("found skill tree data in slot " + slot);
                update.set("skillTreeDataMap." + slot, corePlayerData.getSkillTreeDataMap().get(slot));
            }
        }
        // Update SpellData
        for (int slot = 1; slot < RunicCore.getDataAPI().getMaxCharacterSlot(); slot++) {
            SpellData spellData = corePlayerData.getSpellData(slot);
            if (spellData != null) {
//                Bukkit.getLogger().info("found spell data in slot " + slot);
                update.set("spellDataMap." + slot, corePlayerData.getSpellDataMap().get(slot));
            }
        }
        // Update TitleData
        update.set("titleData", corePlayerData.getTitleData());
        return update;
    }

    /**
     * A task that saves all players with the 'markedForSave:{plugin}' key in redis to mongo.
     * Here's how this works:
     * - Whenever a player's data is written to Jedis, their UUID is added to a set in Jedis
     * - When this task runs, it checks for all players who have not been saved from Jedis --> Mongo and flushes the data, saving each entry
     * - The player is then no longer marked for save.
     */
    @Override
    public void saveAllToMongo(WriteCallback callback) {
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(this::sendBulkOperation)
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to write to Mongo!")
                .syncLast(bulkWriteResult -> {
                    if (bulkWriteResult.wasAcknowledged()) {
                        Bukkit.getLogger().info("RunicCore modified " + bulkWriteResult.getModifiedCount() + " documents.");
                    }
                    callback.onWriteComplete();
                })
                .execute();
    }

    @Override
    public BulkWriteResult sendBulkOperation() {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            Set<String> playersToSave = jedis.smembers(getJedisSet());
            if (playersToSave.isEmpty()) return BulkWriteResult.unacknowledged();
            BulkOperations bulkOperations = RunicCore.getDataAPI().getMongoTemplate().bulkOps(BulkOperations.BulkMode.UNORDERED, getCollectionName());
            for (String uuidString : playersToSave) {
                UUID uuid = UUID.fromString(uuidString);
                // Load their data async with a future
                CorePlayerData corePlayerData = RunicCore.getDataAPI().loadCorePlayerData(uuid);
                // Player is no longer marked for save
                jedis.srem(getJedisSet(), uuid.toString());
                // Find the correct document to update
                bulkOperations.updateOne(getQuery(uuid), getUpdate(corePlayerData));
            }
            return bulkOperations.execute();
        }
    }

    public BukkitTask getTask() {
        return task;
    }

}
