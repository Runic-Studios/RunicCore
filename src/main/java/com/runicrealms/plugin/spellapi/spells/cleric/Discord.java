package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Discord extends Spell {

    private static final int DELAY = 1;
    private static final int DAMAGE_AMT = 8;
    private static final int DURATION = 2;
    private static final int MAX_DIST = 10;
    private static final float RADIUS = 3f;

    public Discord() {
        super("Discord",
                "You target a location within " + MAX_DIST + " blocks, " +
                        "marking it for chaos and discord! After " + DELAY + "s, " +
                        "enemies within " + RADIUS + " blocks are stunned for " +
                        DURATION + "s and suffer " + DAMAGE_AMT + "spellʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 8, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

    }
}
