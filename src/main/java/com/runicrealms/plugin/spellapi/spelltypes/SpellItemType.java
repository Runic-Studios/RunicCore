package com.runicrealms.plugin.spellapi.spelltypes;

public enum SpellItemType {
    NONE("NONE", -1),
    ARTIFACT("Artifact", 0),
    RUNE("Rune", 1);

    private int slot;
    private String name;

    SpellItemType(String name, int slot) {
        this.name = name;
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public String getName() {
        return this.name;
    }

}