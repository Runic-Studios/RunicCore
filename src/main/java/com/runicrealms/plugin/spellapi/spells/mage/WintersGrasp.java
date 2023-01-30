package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;


public class WintersGrasp extends Spell {

    public WintersGrasp() {
        super("Winter's Grasp",
                "If an enemy takes 3 instances of magic damage " +
                        "from you within 3s, they become frozen solid! " +
                        "Frozen enemies are stunned in place for 1.5s! " +
                        "This effect cannot occur on the same target more " +
                        "than once every 6s.",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

}

