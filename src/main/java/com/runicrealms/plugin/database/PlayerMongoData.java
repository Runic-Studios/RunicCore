package com.runicrealms.plugin.database;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import org.bson.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayerMongoData implements MongoData {

    private final String uuid;
    private final Set<MongoSetUpdate> setUpdates;
    private final Set<MongoUnsetUpdate> unsetUpdates;
    private Document document;

    /**
     * Build a reference to the in-memory document of the player and a wrapper for document updates
     *
     * @param uuid of the player
     */
    public PlayerMongoData(String uuid) {
        this.uuid = uuid;
        this.setUpdates = new HashSet<>();
        this.unsetUpdates = new HashSet<>();
        try {
            if (RunicCore.getDataAPI().getPlayerDocumentMap().get(uuid) != null) {
                this.document = RunicCore.getDataAPI().getPlayerDocumentMap().get(uuid);
                // Bukkit.broadcastMessage("loading mongo document from last 30 days");
            } else if (RunicCore.getDataAPI().isInCollection(uuid)) {
                this.document = RunicCore.getDataAPI().retrieveDocumentFromCollection(uuid);
                // Bukkit.broadcastMessage("loading mongo document from collection");
            } else {
                this.document = RunicCore.getDataAPI().addNewDocument(uuid);
                // Bukkit.broadcastMessage("building new mongo document");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerMongoDataSection getCharacter(int slot) {
        return new PlayerMongoDataSection(this, "character." + slot);
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
            if (type == Integer.class && element instanceof String)
                return type.cast(Integer.parseInt((String) element));
            if (type == Short.class && element instanceof String) return type.cast(Short.parseShort((String) element));
            if (type == Long.class && element instanceof String) return type.cast(Long.parseLong((String) element));
            if (type == Byte.class && element instanceof String) return type.cast(Byte.parseByte((String) element));
            if (type == Double.class && element instanceof String)
                return type.cast(Double.parseDouble((String) element));
            if (type == Float.class && element instanceof String) return type.cast(Float.parseFloat((String) element));
            if (type == Boolean.class && element instanceof String)
                return type.cast(Boolean.parseBoolean((String) element));
            return (T) element;
        }
        Object element = this.document.get(key);
        if (type == Integer.class && element instanceof String) return type.cast(Integer.parseInt((String) element));
        if (type == Short.class && element instanceof String) return type.cast(Short.parseShort((String) element));
        if (type == Long.class && element instanceof String) return type.cast(Long.parseLong((String) element));
        if (type == Byte.class && element instanceof String) return type.cast(Byte.parseByte((String) element));
        if (type == Double.class && element instanceof String) return type.cast(Double.parseDouble((String) element));
        if (type == Float.class && element instanceof String) return type.cast(Float.parseFloat((String) element));
        if (type == Boolean.class && element instanceof String)
            return type.cast(Boolean.parseBoolean((String) element));
        return (T) element;
    }

    @Override
    public void refresh() {
        MongoDatabase mongoDatabase = RunicCore.getDataAPI().getMongoDatabase();
        this.document = mongoDatabase.getCollection("player_data").find(Filters.eq("player_uuid", this.uuid)).first();
        RunicCore.getDataAPI().getPlayerDocumentMap().put(this.uuid, this.document);
    }

    @Override
    public void save() {
        MongoDatabase mongoDatabase = RunicCore.getDataAPI().getMongoDatabase();
        if (this.unsetUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoUnsetUpdate update : this.unsetUpdates) {
                updates.append(update.getKey(), "");
            }
            mongoDatabase.getCollection("player_data").updateOne(new Document("player_uuid", this.uuid), new Document("$unset", updates));
            this.unsetUpdates.clear();
        }
        if (this.setUpdates.size() > 0) {
            BasicDBObject updates = new BasicDBObject();
            for (MongoSetUpdate update : this.setUpdates) {
                updates.append(update.getKey(), update.getValue());
            }
            mongoDatabase.getCollection("player_data").updateOne(new Document("player_uuid", this.uuid), new Document("$set", updates));
            this.setUpdates.clear();
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

}
