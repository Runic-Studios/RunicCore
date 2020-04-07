package com.runicrealms.plugin.database;

public interface IRunicDatabase {

    // TODO: getPlayerBank

    PlayerMongoData getPlayerFile(String uuid); // lookup a player's json data file

    GuildMongoData getGuildFile(String guildPrefix);

    //PlayerCache getPlayerCache(Document playerFile); // returns the in-memory object of the player
}
