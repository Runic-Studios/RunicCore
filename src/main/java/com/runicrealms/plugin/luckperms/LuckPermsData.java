package com.runicrealms.plugin.luckperms;

import java.util.Set;

public interface LuckPermsData {

    Set<String> getKeys();

    boolean containsKey(String key);

    String getString(String key);

    int getInteger(String key);

    double getDouble(String key);

    float getFloat(String key);

    boolean getBoolean(String key);

    Object get(String key);

    void set(String key, Object object);

    void add(LuckPermsData newData);

}
