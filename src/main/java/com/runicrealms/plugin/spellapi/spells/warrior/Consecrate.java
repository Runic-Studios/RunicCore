package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class Consecrate extends Spell {

    public Consecrate() {
        super("Consecrate", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("?");
    }


}
