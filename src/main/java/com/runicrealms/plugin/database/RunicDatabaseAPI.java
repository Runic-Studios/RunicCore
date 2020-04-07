package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;
import org.bson.conversions.Bson;

public class RunicDatabaseAPI implements IRunicDatabase {

    private CharacterAPI characterAPI;

    public RunicDatabaseAPI() {
        characterAPI = new CharacterAPI();
    }

    @Override
    public Document getPlayerFile(String uuid) {
        return RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", uuid)).first();
    }

    @Override
    public Document getGuildFile(String guildPrefix) {
        return RunicCore.getDatabaseManager().getGuildData().find((Filters.eq("prefix", guildPrefix))).first();
    }

    @Override
    public PlayerCache getPlayerCache(Document playerFile) {
        return null;
    }

    public CharacterAPI getCharacterAPI() {
        return characterAPI;
    }

    @Override
    public void updateDocumentField(String uuid, String identifier, Object value) {
        Document document = new Document("player_uuid", uuid); // change player_uuid to uuid
        Bson newValue = new Document(identifier, value);
        Bson updateOperation = new Document("$set", newValue);
        RunicCore.getDatabaseManager().getPlayerData().updateOne(document, updateOperation);
    }
}
