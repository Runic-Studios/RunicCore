package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

public class Charged extends Spell {

    public Charged() {
        super("Charged",
                "Every time you cast a spell gain (1 + 0.1x lvl) INT." +
                        "This effect can stack up to 5 times." +
                        "After not casting a spell for 4 seconds, remove all stacks." +
                        "When fully charged, you glow brightly!",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }
}

