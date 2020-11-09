package com.runicrealms.plugin.spellapi.spelltypes;

public enum StatEnum {

    INTELLIGENCE("INT"),
    STRENGTH("STR");

    private final String shorthand;

    StatEnum(String shorthand) {
        this.shorthand = shorthand;
    }

    public String getShorthand() {
        return shorthand;
    }

}
