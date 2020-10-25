package com.runicrealms.plugin.database;

import java.util.Set;

public interface Data {

    void set(String key, Object value);

    Object get(String key);

    <T> T get(String key, Class<T> type);

    void save();

    boolean has(String key);

    void remove(String key);

    Data getSection(String root);

    Set<String> getKeys();

}
