package com.runicrealms.plugin.model;

import java.util.Map;

public interface JedisSerializable {
    Map<String, String> toMap();
}
