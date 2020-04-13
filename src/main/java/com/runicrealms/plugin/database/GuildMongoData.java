package com.runicrealms.plugin.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GuildMongoData implements MongoData {

    private Document document;
    private String prefix;
    private Set<MongoSetUpdate> setUpdates;
    private Set<MongoUnsetUpdate> unsetUpdates;

    public GuildMongoData(String prefix) {
        this.prefix = prefix;
        this.setUpdates = new HashSet<MongoSetUpdate>();
        this.unsetUpdates = new HashSet<MongoUnsetUpdate>();
        this.document = RunicCore.getDatabaseManager().getGuildData().find(
                (Filters.eq("prefix", prefix))).first();
        if (this.document == null) {
            this.document = new Document("prefix", this.prefix);
            RunicCore.getDatabaseManager().getGuildData().insertOne(this.document);
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
        this.document = RunicCore.getDatabaseManager().getGuildData().find(
                (Filters.eq("prefix", prefix))).first();
    }

    @Override
    public void save() {
        if (this.setUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoSetUpdate update : this.setUpdates) {
                updates.append(update.getKey(), update.getValue());
            }
            RunicCore.getDatabaseManager().getGuildData().updateOne(new Document("prefix", this.prefix), new Document("$set", updates));
            this.setUpdates.clear();
        }
        if (this.unsetUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoUnsetUpdate update : this.unsetUpdates) {
                updates.append(update.getKey(), "");
            }
            RunicCore.getDatabaseManager().getGuildData().updateOne(new Document("prefix", this.prefix), new Document("$unset", updates));
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
        return new GuildMongoDataSection(this, root);
    }

    @Override
    public Set<MongoSetUpdate> getSetUpdates() {
        return null;
    }

    @Override
    public Set<MongoUnsetUpdate> getUnsetUpdates() {
        return null;
    }

    @Override
    public Set<String> getKeys() {
        return this.document.keySet();
    }

    @Override
    public String getIdentifier() {
        return return this.prefix;
    }

}
