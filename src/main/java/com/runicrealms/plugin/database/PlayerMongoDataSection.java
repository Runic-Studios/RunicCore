package com.runicrealms.plugin.database;

import org.bson.Document;

public class PlayerMongoDataSection implements MongoDataSection {

    private Document document;
    private PlayerMongoData parent;
    private String root;

    public PlayerMongoDataSection(PlayerMongoData parent, String key) {
        this.document = (Document) parent.get(key);
        this.parent = parent;
        this.root = key;
    }

    @Override
    public void set(String key, Object value) {
        this.parent.getUpdates().add(new Document("$set", new Document(this.root + "." + key, value)));
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
    public Document getDocument() {
        return this.document;
    }

    @Override
    public boolean has(String key) {
        return this.document.containsKey(key);
    }

    @Override
    public void remove(String key) {
        this.parent.getUpdates().add(new Document("$unset", new Document(this.root + "." + key, "")));
    }

    @Override
    public MongoData getParentData() {
        return this.parent;
    }

    @Override
    public void saveParent() {
        this.parent.save();
    }

}