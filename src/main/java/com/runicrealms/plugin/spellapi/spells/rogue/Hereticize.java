package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class Hereticize extends Spell {

    public Hereticize() {
        super("Hereticize", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("?");
    }

}
