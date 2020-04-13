package com.runicrealms.plugin.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayerMongoData implements MongoData {

    private Document document;
    private String uuid;
    private Set<MongoSetUpdate> setUpdates;
    private Set<MongoUnsetUpdate> unsetUpdates;

    public PlayerMongoData(String uuid) {
        this.uuid = uuid;
        this.setUpdates = new HashSet<>();
        this.unsetUpdates = new HashSet<>();
        this.document = RunicCore.getDatabaseManager().getPlayerData().find(Filters.eq("player_uuid", this.uuid)).first();
        if (this.document == null) {
            this.document = new Document("player_uuid", this.uuid);
            RunicCore.getDatabaseManager().getPlayerData().insertOne(this.document);
        }
    }

    @Override
    public void set(String key, Object value) {
        this.setUpdates.add(new MongoSetUpdate(key, value));
    }

    @Override
    public Object get(String key) {
        if (key.contains(".")) {
            return this.document.getEmbedded(Arrays.asList(key.split("\\.")), Object.class);
        }
        return this.document.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        if (key.contains(".")) {
            return this.document.getEmbedded(Arrays.asList(key.split("\\.")), type);
        }
        return this.document.get(key, type);
    }

    @Override
    public void refresh() {
        this.document = RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", this.uuid)).first();
    }

    @Override
    public void save() {
        if (this.setUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoSetUpdate update : this.setUpdates) {
                updates.append(update.getKey(), update.getValue());
            }
            RunicCore.getDatabaseManager().getPlayerData().updateOne(new Document("player_uuid", this.uuid), new Document("$set", updates));
            this.setUpdates.clear();
        }
        if (this.unsetUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoUnsetUpdate update : this.unsetUpdates) {
                updates.append(update.getKey(), "");
            }
            RunicCore.getDatabaseManager().getPlayerData().updateOne(new Document("player_uuid", this.uuid), new Document("$unset", updates));
            this.unsetUpdates.clear();
        }
    }

    @Override
    public Document getDocument() {
        return this.document;
    }

    @Override
    public boolean has(String key) {
        return this.get(key) != null;
    }

    @Override
    public void remove(String key) {
        this.unsetUpdates.add(new MongoUnsetUpdate(key));
    }

    @Override
    public MongoDataSection getSection(String root) {
        return new PlayerMongoDataSection(this, root);
    }

    @Override
    public Set<MongoSetUpdate> getSetUpdates() {
        return this.setUpdates;
    }

    @Override
    public Set<MongoUnsetUpdate> getUnsetUpdates() {
        return this.unsetUpdates;
    }

    @Override
    public Set<String> getKeys() {
        return this.document.keySet();
    }

    @Override
    public String getIdentifier() {
        return this.uuid;
    }

    public PlayerMongoDataSection getCharacter(int slot) {
        return new PlayerMongoDataSection(this, "character." + slot);
    }

}
