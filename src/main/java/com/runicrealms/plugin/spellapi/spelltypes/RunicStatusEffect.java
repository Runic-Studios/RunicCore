package com.runicrealms.plugin.spellapi.spelltypes;

public enum RunicStatusEffect {
    DISARM(false),
    INVULNERABILITY(true),
    ROOT(false),
    SILENCE(false),
    STUN(false);

    private final boolean buff; // true if the effect is a boon

    RunicStatusEffect(boolean isBuff) {
        this.buff = isBuff;
    }

    public boolean isBuff() {
        return buff;
    }
}
