package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

public class HolyFire extends Spell {
    private static final int MAX_STACKS = 5;
    private static final int STACK_DURATION = 12;
    private static final double PERCENT = .10;

    public HolyFire() {
        super("Holy Fire",
                "Each time you land your &aSmite &7spell, you gain " +
                        "a stack of Holy Fire! For each stack, you gain " + (int) (PERCENT * 100) +
                        "% of your total &eWisdom✸ &7as increased healing! " +
                        "Each &aSmite &7refreshes the duration " +
                        "of your stacks. While at max stacks, " +
                        "you glow bright with holy power!" +
                        "\nMax stacks: " + MAX_STACKS + "\nStacks expiry: " + STACK_DURATION + "s",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {

    }
}

