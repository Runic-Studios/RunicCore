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
    private volatile Set<MongoSetUpdate> setUpdates;
    private volatile Set<MongoUnsetUpdate> unsetUpdates;

    public GuildMongoData(String prefix) {
        this.prefix = prefix;
        this.setUpdates = new HashSet<>();
        this.unsetUpdates = new HashSet<>();
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
            Object element = this.document.getEmbedded(Arrays.asList(key.split("\\.")), Object.class);
            if (type == Integer.class && element instanceof String) return type.cast(Integer.parseInt((String) element));
            if (type == Short.class && element instanceof String) return type.cast(Short.parseShort((String) element));
            if (type == Long.class && element instanceof String) return type.cast(Long.parseLong((String) element));
            if (type == Byte.class && element instanceof String) return type.cast(Byte.parseByte((String) element));
            if (type == Double.class && element instanceof String) return type.cast(Double.parseDouble((String) element));
            if (type == Float.class && element instanceof String) return type.cast(Float.parseFloat((String) element));
            if (type == Boolean.class && element instanceof String) return type.cast(Boolean.parseBoolean((String) element));
            return (T) element;
        }
        Object element = this.document.get(key);
        if (type == Integer.class && element instanceof String) return type.cast(Integer.parseInt((String) element));
        if (type == Short.class && element instanceof String) return type.cast(Short.parseShort((String) element));
        if (type == Long.class && element instanceof String) return type.cast(Long.parseLong((String) element));
        if (type == Byte.class && element instanceof String) return type.cast(Byte.parseByte((String) element));
        if (type == Double.class && element instanceof String) return type.cast(Double.parseDouble((String) element));
        if (type == Float.class && element instanceof String) return type.cast(Float.parseFloat((String) element));
        if (type == Boolean.class && element instanceof String) return type.cast(Boolean.parseBoolean((String) element));
        return (T) element;
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
        this.refresh();
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
        return this.prefix;
    }

}
