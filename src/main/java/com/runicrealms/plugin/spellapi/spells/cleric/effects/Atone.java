package com.runicrealms.plugin.spellapi.spells.cleric.effects;

import com.runicrealms.plugin.spellapi.SpellEffect;

public class Atone implements SpellEffect {

    @Override
    public String getName() {
        return "Atone";
    }

    @Override
    public boolean isBuff() {
        return false;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public double getDuration() {
        return 0;
    }
}
