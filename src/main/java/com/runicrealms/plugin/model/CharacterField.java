package com.runicrealms.plugin.model;

public enum CharacterField {

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

    CharacterField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    /**
     * Returns the corresponding RedisField from the given string version
     *
     * @param field a string matching a constant
     * @return the constant
     */
    public static CharacterField getFromFieldString(String field) {
        for (CharacterField characterField : CharacterField.values()) {
            if (characterField.getField().equalsIgnoreCase(field))
                return characterField;
        }
        return null;
    }
}
