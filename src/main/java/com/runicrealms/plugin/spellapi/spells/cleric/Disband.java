package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

public class Disband extends Spell {
    private static final int ATTACKS_TO_TRIGGER = 3;
    private static final int COOLDOWN = 7;
    private static final int DURATION = 3;
    private static final double THRESHOLD = 2.5;

    public Disband() {
        super("Disband",
                "Damaging an enemy " + ATTACKS_TO_TRIGGER + " times with basic attacks " +
                        "within " + THRESHOLD + "s causes you to disarm the enemy for " + DURATION + "s! " +
                        "Disarmed enemies are unable to deal damage with basic attacks. " +
                        "This effect cannot occur more than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

}

