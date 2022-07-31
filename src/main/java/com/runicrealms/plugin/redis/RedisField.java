package com.runicrealms.plugin.redis;

public enum RedisField {

    SLOT("slot"),
    CURRENT_HEALTH("currentHp"),
    STORED_HUNGER("storedHunger"),
    PLAYER_UUID("playerUuid"),
    LOCATION("location"),
    CLASS_TYPE("classType"),
    CLASS_EXP("exp"),
    CLASS_LEVEL("level"),
    OUTLAW_ENABLED("outlawEnabled"),
    OUTLAW_RATING("outlawRating"),
    PROF_NAME("profName"),
    PROF_EXP("profExp"),
    PROF_LEVEL("profLevel"),
    GUILD("guild");

    private final String field;

    RedisField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
