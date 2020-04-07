package com.runicrealms.plugin.database;

import org.bson.Document;

public interface MongoDataSection {

    public void set(String key, Object value);

    public Object get(String key);

    public <T> T get(String key, Class<T> type);

    public Document getDocument();

    public boolean has(String key);

    public void remove(String key);

    public MongoData getParentData();

    public void saveParent();

}
