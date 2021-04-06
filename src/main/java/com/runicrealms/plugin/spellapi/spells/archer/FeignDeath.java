package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

@SuppressWarnings("FieldCanBeLocal")
public class FeignDeath extends Spell {

    public FeignDeath() {
        super("Feign Death",
                "?",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
    }
}
