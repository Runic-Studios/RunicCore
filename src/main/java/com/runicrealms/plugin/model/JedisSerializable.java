package com.runicrealms.plugin.model;

import java.util.Map;

public interface JedisSerializable {

    Map<String, String> toMap();

    <T> T fromMap(String key, Class<T> type);
}
