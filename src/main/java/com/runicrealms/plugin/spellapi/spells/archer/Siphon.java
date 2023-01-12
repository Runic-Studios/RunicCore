package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

public class Siphon extends Spell {

    public Siphon() {
        super("Siphon",
                "?",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }
}

