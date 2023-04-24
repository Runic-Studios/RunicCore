package com.runicrealms.plugin.model;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.TitleAPI;
import com.runicrealms.plugin.api.WriteCallback;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class TitleManager implements Listener, TitleAPI {

    public TitleManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }


    @Override
    public TitleData checkRedisForTitleData(UUID uuid, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        if (jedis.exists(database + ":" + uuid.toString() + ":" + TitleData.DATA_SECTION_PREFIX)) {
            return new TitleData(uuid, jedis);
        }
        return null;
    }

    @Override
    public TitleData getTitleData(UUID uuid) {
        // Step 1: Check if title data is memoized
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        if (corePlayerData != null) {
            return corePlayerData.getTitleData();
        }
        return null;
    }

    @Override
    public TitleData loadTitleData(UUID uuid, Jedis jedis) {
        // Step 1: Check if title data is cached in redis
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        TitleData titleDataRedis = checkRedisForTitleData(uuid, jedis);
        if (titleDataRedis != null) {
            corePlayerData.setTitleData(titleDataRedis);
            return titleDataRedis;
        }
        // Step 2: Check the mongo database. First, we find our top-level document
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("titleData");
        TitleData titleDataMongo = RunicCore.getDataAPI().getMongoTemplate().findOne(query, TitleData.class);
        if (titleDataMongo != null) {
            corePlayerData.setTitleData(titleDataMongo);
            titleDataMongo.writeToJedis(uuid, jedis);
            return titleDataMongo;
        }
        // Step 3: Create new and add to in-memory object
        TitleData newData = new TitleData();
        corePlayerData.setTitleData(newData);
        return newData;
    }

    @Override
    public void removePrefixesAndSuffixes(Player player, WriteCallback callback) {
        UUID uuid = player.getUniqueId();
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(() -> {
                    try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                        TitleData titleData = loadTitleData(uuid, jedis);
                        titleData.setSuffix("");
                        titleData.setPrefix("");
                        titleData.writeToJedis(uuid, jedis);
                        return titleData;
                    }
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to clear title prefixes/suffixes!")
                .syncLast(titleData -> {
                    callback.onWriteComplete();
                    player.sendMessage(ChatColor.YELLOW + "Your titles have been reset!");
                })
                .execute();
    }

}
