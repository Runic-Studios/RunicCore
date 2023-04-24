package com.runicrealms.plugin.model;

public enum SpellField {

    HOT_BAR_ONE("hotBarOne"),
    LEFT_CLICK("leftClick"),
    RIGHT_CLICK("rightClick"),
    SWAP_HANDS("swapHands");

    private final String field;

    SpellField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
