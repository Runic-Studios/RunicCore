package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NetTrap extends Spell {

    private static final int DURATION = 12;
    private static final int RADIUS = 2;
    private static final int STUN_DURATION = 2;
    private static final double WARMUP = 0.5; // seconds

    public NetTrap() {
        super("Net Trap",
                "You lay down a trap, which arms after " + WARMUP +
                        "s and lasts for " + DURATION +
                        "s. The first enemy to step over the trap triggers it, " +
                        "causing all enemies within " + RADIUS +
                        " blocks to be lifted into their air and stunned for " +
                        STUN_DURATION + "s! Mobs caught in the trap take " +
                        ChatColor.BOLD + ChatColor.GRAY + "double " + ChatColor.GRAY +
                        "damage for the duration!",
                ChatColor.WHITE, CharacterClass.ARCHER, 15, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
    }

}
