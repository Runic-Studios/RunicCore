package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;

import java.util.Map;

public interface JedisSerializable {

    Map<String, String> toMap();

    void writeToMongo(PlayerMongoData playerMongoData);
}
