package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TitleManager implements Listener {

    private final Map<UUID, TitleData> titleDataMap;

    public TitleManager() {
        titleDataMap = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler
    public void onCharacterSelect(CharacterSelectEvent event) {
        loadTitleData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        TitleData titleData = loadTitleData(event.getPlayer().getUniqueId());
        titleData.writeToJedis(event.getJedis());
    }

    @EventHandler
    public void onMongoSave(MongoSaveEvent event) {
        TitleData titleData;
        for (UUID uuid : event.getPlayersToSave().keySet()) {
            titleData = loadTitleData(uuid);
            PlayerMongoData playerMongoData = event.getPlayersToSave().get(uuid).getPlayerMongoData();
            titleData.writeToMongo(playerMongoData);
        }
    }

    /**
     * Tries to retrieve a TitleData object from server memory, otherwise falls back to redis / mongo
     *
     * @param uuid of the player
     * @return a TitleData object
     */
    public TitleData loadTitleData(UUID uuid) {
        // Step 1: check if title data is memoized
        TitleData titleData = titleDataMap.get(uuid);
        if (titleData != null) {
            // Bukkit.broadcastMessage("memoized data found, building title from memory");
            return titleData;
        }
        // Step 2: check if title data is cached in redis
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            return loadTitleData(uuid, jedis);
        }
    }

    /**
     * Creates a TitleData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     */
    public TitleData loadTitleData(UUID uuid, Jedis jedis) {
        // Step 2: check if title data is cached in redis
        TitleData titleData = checkRedisForTitleData(uuid, jedis);
        if (titleData != null) return titleData;
        // Step 2: check mongo documents
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        return new TitleData(uuid, playerMongoData, jedis);
    }

    /**
     * Checks redis to see if the currently selected character's title data is cached.
     * And if it is, returns the Title object
     *
     * @param uuid  of player to check
     * @param jedis the jedis resource
     * @return a TitleData object if it is found in redis
     */
    public TitleData checkRedisForTitleData(UUID uuid, Jedis jedis) {
        if (jedis.exists(uuid.toString() + ":" + TitleData.DATA_SECTION_PREFIX)) {
            return new TitleData(uuid, jedis);
        }
        return null;
    }

    public Map<UUID, TitleData> getTitleDataMap() {
        return titleDataMap;
    }
}
