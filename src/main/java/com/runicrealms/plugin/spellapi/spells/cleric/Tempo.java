package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

/**
 * New bard ultimate passive
 *
 * @author BoBoBalloon
 */
public class Tempo extends Spell {
    public Tempo() {
        super("Tempo", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("");
    }
}
