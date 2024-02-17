package com.runicrealms.plugin.spellapi;

public enum SpellSlot {
    HOT_BAR_ONE("hotBarOne"),
    LEFT_CLICK("leftClick"),
    RIGHT_CLICK("rightClick"),
    SWAP_HANDS("swapHands");

    private final String field;

    SpellSlot(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
