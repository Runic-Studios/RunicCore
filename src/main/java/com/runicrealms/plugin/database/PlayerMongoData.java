package com.runicrealms.plugin.database;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.Set;

public class PlayerMongoData implements MongoData {

    private Document document;
    private String uuid;
    private Set<Bson> updates;

    public PlayerMongoData(String uuid) {
        this.uuid = uuid;
        this.updates = new HashSet<>();
        this.document = RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", this.uuid)).first();
    }

    @Override
    public void set(String key, Object value) {
        this.updates.add(new Document("$set", new Document(key, value)));
    }

    @Override
    public Object get(String key) {
        return this.document.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        return (T) this.document.get(key);
    }

    @Override
    public void refresh() {
        this.document = RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", this.uuid)).first();
    }

    @Override
    public void save() {
        for (Bson bson : this.updates) {
            RunicCore.getDatabaseManager().getPlayerData().updateOne(new Document("player_uuid", this.uuid), bson);
        }
        this.updates.clear();
    }

    @Override
    public Document getDocument() {
        return this.document;
    }

    @Override
    public boolean has(String key) {
        return this.document.containsKey(key);
    }

    @Override
    public void remove(String key) {
        this.updates.add(new Document("$unset", new Document(key, "")));
    }

}
