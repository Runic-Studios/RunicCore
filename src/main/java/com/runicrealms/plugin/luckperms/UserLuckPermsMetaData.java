package com.runicrealms.plugin.luckperms;

import com.runicrealms.plugin.common.api.LuckPermsData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserLuckPermsMetaData implements LuckPermsData {

    private final Map<String, String> data;

    public UserLuckPermsMetaData() {
        this.data = new HashMap<>();
    }

    public UserLuckPermsMetaData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public Set<String> getKeys() {
        return data.keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public String getString(String key) {
        return data.get(key);
    }

    @Override
    public int getInteger(String key) {
        try {
            return Integer.parseInt(data.get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Cannot retrieve key \"" + key + "\": value \"" + data.get(key) + "\" is not an integer!");
        }
    }

    @Override
    public double getDouble(String key) {
        try {
            return Double.parseDouble(data.get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Cannot retrieve key \"" + key + "\": value \"" + data.get(key) + "\" is not a double!");
        }
    }

    @Override
    public float getFloat(String key) {
        try {
            return Float.parseFloat(data.get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Cannot retrieve key \"" + key + "\": value \"" + data.get(key) + "\" is not a float!");
        }
    }

    @Override
    public boolean getBoolean(String key) {
        String value = data.get(key);
        if (value.equalsIgnoreCase("1")) return true;
        if (value.equalsIgnoreCase("0")) return false;
        try {
            return Boolean.parseBoolean(data.get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Cannot retrieve key \"" + key + "\": value \"" + data.get(key) + "\" is not a boolean!");
        }
    }

    @Override
    public long getLong(String key) {
        try {
            return Long.parseLong(data.get(key));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Cannot retrieve key \"" + key + "\": value \"" + data.get(key) + "\" is not a long!");
        }
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public void set(String key, Object object) {
        if (object instanceof Boolean) {
            data.put(key, (Boolean) object ? "1" : "0");
        } else {
            data.put(key, object.toString());
        }
    }

    @Override
    public void add(LuckPermsData newData) {
        for (String key : newData.getKeys()) {
            data.put(key, newData.get(key).toString());
        }
    }
}