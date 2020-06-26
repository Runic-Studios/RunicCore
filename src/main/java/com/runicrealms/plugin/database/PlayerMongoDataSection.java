package com.runicrealms.plugin.database;

import org.bson.Document;

import java.util.Arrays;
import java.util.Set;

public class PlayerMongoDataSection implements MongoDataSection {

    private Document document;
    private final PlayerMongoData parent;
    private final String root;

    public PlayerMongoDataSection(PlayerMongoData parent, String key) {
        this.document = (Document) parent.get(key);
        this.parent = parent;
        this.root = key;
    }

    @Override
    public void set(String key, Object value) {
        this.parent.getSetUpdates().add(new MongoSetUpdate(this.root + "." + key, value));
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
    public Document getDocument() {
        return this.document;
    }

    @Override
    public boolean has(String key) {
        return this.get(key) != null;
    }

    @Override
    public void remove(String key) {
        this.parent.getUnsetUpdates().add(new MongoUnsetUpdate(this.root + "." + key));
    }

    @Override
    public MongoData getParentData() {
        return this.parent;
    }

    @Override
    public void saveParent() {
        this.parent.save();
        this.refresh();
    }

    @Override
    public Set<String> getKeys() {
        return this.document.keySet();
    }

    @Override
    public MongoDataSection getSection(String root) {
        return new PlayerMongoDataSection(this.parent, this.root + "." + root);
    }

    @Override
    public void refresh() {
        this.document = (Document) this.parent.get(this.root);
    }

}