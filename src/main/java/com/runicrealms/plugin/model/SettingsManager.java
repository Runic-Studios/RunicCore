package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SettingsAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class SettingsManager implements Listener, SettingsAPI {

    public SettingsManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }


    @Override
    public SettingsData checkRedisForSettingsData(UUID uuid, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        if (jedis.exists(database + ":" + uuid.toString() + ":" + SettingsData.DATA_SECTION_PREFIX)) {
            return new SettingsData(uuid, jedis);
        }
        return null;
    }

    @Override
    public SettingsData loadSettingsData(UUID uuid, Jedis jedis) {
        // Step 1: Check if settings data is cached in redis
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getCorePlayerData(uuid);
        SettingsData settingsData = checkRedisForSettingsData(uuid, jedis);
        if (settingsData != null) {
            Bukkit.broadcastMessage("SETTINGS DATA FOUND REDIS");
            corePlayerData.setSettingsData(settingsData);
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
            return settingsDataMongo;
        }
        Bukkit.broadcastMessage("CREATING NEW SETTINGS DATA");
        // Step 3: Create new and add to in-memory object
        SettingsData newData = new SettingsData();
        corePlayerData.setSettingsData(newData);
        corePlayerData.writeToJedis(jedis);
        return newData;
    }

}
