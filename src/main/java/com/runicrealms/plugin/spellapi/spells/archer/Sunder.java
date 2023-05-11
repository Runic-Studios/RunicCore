package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class Sunder extends Spell {

    public Sunder() {
        super("Sunder", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("?");
    }

}
