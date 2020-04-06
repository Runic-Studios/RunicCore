package com.runicrealms.plugin.database;

import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;

public interface IRunicDatabase {

    // TODO: getPlayerBank

    // TODO: getPlayerGuild

    // TODO: getPlayerTradeMarket

    Document getPlayerFile(String uuid); // lookup a player's json data file

    PlayerCache getPlayerCache(Document playerFile); // returns the in-memory object of the player

    void updateDocumentField(String uuid, String identifier, Object value);
}
