package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager implements Listener, SessionDataManager {
    private final Map<UUID, SettingsData> settingsDataMap = new HashMap<>();

    public SettingsManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public SessionDataRedis checkRedisForSessionData(Object identifier, Jedis jedis, int... slot) {
        UUID uuid = (UUID) identifier;
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        if (jedis.exists(database + ":" + uuid.toString() + ":" + SettingsData.DATA_SECTION_SETTINGS)) {
            return new SettingsData(uuid, jedis);
        }
        return null;
    }

    @Override
    public SessionDataRedis getSessionData(Object identifier, int... slot) {
        UUID uuid = (UUID) identifier;
        if (uuid == null) return null;
        return settingsDataMap.get(uuid);
    }

    @Override
    public SessionDataRedis loadSessionData(Object identifier, Jedis jedis, int... slot) {
        UUID uuid = (UUID) identifier;
        // Step 0: Check if settings are stored in-memory
        if (settingsDataMap.get(uuid) != null)
            return settingsDataMap.get(uuid);
        // Step 1: Check if settings data is cached in redis
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        SettingsData settingsData = (SettingsData) checkRedisForSessionData(uuid, jedis);
        if (settingsData != null) {
            Bukkit.broadcastMessage("SETTINGS DATA FOUND REDIS");
            corePlayerData.setSettingsData(settingsData);
            settingsDataMap.put(uuid, settingsData);
            return settingsData;
        }
        // Step 2: Check the mongo database. First, we find our top-level document
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
        // Project only the fields we need
        query.fields().include("settingsData");
        SettingsData settingsDataMongo = RunicCore.getDataAPI().getMongoTemplate().findOne(query, SettingsData.class);
        if (settingsDataMongo != null) {
            Bukkit.broadcastMessage("SETTINGS DATA FOUND MONGO");
            corePlayerData.setSettingsData(settingsDataMongo);
            settingsDataMongo.writeToJedis(uuid, jedis);
            settingsDataMap.put(uuid, settingsDataMongo);
            return settingsDataMongo;
        }
        Bukkit.broadcastMessage("CREATING NEW SETTINGS DATA");
        // Step 3: Create new and add to in-memory object
        SettingsData newData = new SettingsData();
        corePlayerData.setSettingsData(newData);
        corePlayerData.writeToJedis(jedis);
        settingsDataMap.put(uuid, newData);
        return newData;
    }

    @EventHandler
    public void onSelect(CharacterSelectEvent event) {
        // Ensure session data cached in-memory
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                loadSessionData(event.getPlayer().getUniqueId(), jedis);
            }
        });
    }

}
