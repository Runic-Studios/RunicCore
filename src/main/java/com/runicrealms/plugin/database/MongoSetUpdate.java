package com.runicrealms.plugin.database;

public class MongoSetUpdate {

    private String key;
    private Object value;

    public MongoSetUpdate(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

}
