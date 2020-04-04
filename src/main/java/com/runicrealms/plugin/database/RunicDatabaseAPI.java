package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;

public class RunicDatabaseAPI implements IRunicDatabase {

    @Override
    public Document getPlayerFile(String uuid) {
        return RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", "2343243243243")).first();
    }

    @Override
    public PlayerCache getPlayerCache(Document playerFile) {
        return null;
    }
}
