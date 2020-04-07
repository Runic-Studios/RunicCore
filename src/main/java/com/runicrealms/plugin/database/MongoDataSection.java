package com.runicrealms.plugin.database;

import org.bson.Document;

import java.util.Set;

public interface MongoDataSection {

    public void set(String key, Object value);

    public Object get(String key);

    public <T> T get(String key, Class<T> type);

    public Document getDocument();

    public boolean has(String key);

    public void remove(String key);

    public MongoData getParentData();

    public void saveParent();

    public Set<String> getKeys();

}
