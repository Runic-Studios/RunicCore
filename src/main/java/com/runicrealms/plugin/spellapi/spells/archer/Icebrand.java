package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

@SuppressWarnings("FieldCanBeLocal")
public class Icebrand extends Spell {

    public Icebrand() {
        super("Icebrand",
                "?",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
    }
}
