package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Surge extends Spell {

    public Surge() {
        super("Surge",
                "You surge forward then upwards, leaving a trail " +
                        "of lightning behind you! Enemies who step in the trail " +
                        "take 30 + (1.5 x lvl) magic damage per second. " +
                        "The trail lasts for 3s.",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }
}

