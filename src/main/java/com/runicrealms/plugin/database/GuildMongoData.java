package com.runicrealms.plugin.database;

import com.runicrealms.plugin.RunicCore;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.HashSet;
import java.util.Set;

public class GuildMongoData implements MongoData {

    private Document document;
    private String prefix;
    private Set<Bson> updates;

    public GuildMongoData(String uuid) {
        this.prefix = prefix;
        this.updates = new HashSet<Bson>();
        this.document = RunicCore.getDatabaseManager().getAPI().getGuildFile(this.prefix);
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
        this.document = RunicCore.getDatabaseManager().getAPI().getGuildFile(this.prefix);
    }

    @Override
    public void save() {
        for (Bson bson : this.updates) {
            RunicCore.getDatabaseManager().getPlayerData().updateOne(new Document("prefix", this.prefix), bson);
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

}
