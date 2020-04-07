package com.runicrealms.plugin.database;

public class RunicDatabaseAPI implements IRunicDatabase {

    //private CharacterAPI characterAPI;

//    public RunicDatabaseAPI() {
//        characterAPI = new CharacterAPI();
//    }

    @Override
    public PlayerMongoData getPlayerFile(String uuid) {
        return new PlayerMongoData(uuid);
    }

    @Override
    public GuildMongoData getGuildFile(String guildPrefix) {
        return new GuildMongoData(guildPrefix);
    }

//    @Override
//    public PlayerCache getPlayerCache(Document playerFile) {
//        return null;
//    }

//    public CharacterAPI getCharacterAPI() {
//        return characterAPI;
//    }
}
