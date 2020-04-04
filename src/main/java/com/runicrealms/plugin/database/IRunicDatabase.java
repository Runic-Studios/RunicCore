package com.runicrealms.plugin.database;

import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;

public interface IRunicDatabase {

    Document getPlayerFile(String uuid); // lookup a player's json data file

    PlayerCache getPlayerCache(Document playerFile); // returns the in-memory object of the player
}
