package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoDataSection;

import java.util.Map;

public interface JedisSerializable {

    Map<String, String> toMap();

    void writeToMongo(PlayerMongoDataSection character);
}
