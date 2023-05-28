package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class WreckingBall extends Spell {

    public WreckingBall() {
        super("Wrecking Ball", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Every time you apply a crowd control " +
                "effect to an enemy, gain a shield equal " +
                "to 10 + (0.2xVIT). This shield can stack " +
                "up to an amount of 33% your total HP. " +
                "This shield lasts until the player is out of combat. ");
    }

}

