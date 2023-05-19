package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class Castigate extends Spell {

    public Castigate() {
        super("Castigate", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("?");
    }

}
