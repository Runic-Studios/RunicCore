package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class Earthquake extends Spell {

    public Earthquake() {
        super("Earthquake", CharacterClass.WARRIOR);
        this.setDescription("You cause tremors to run through the ground under your feet! " +
                "For the next 4s, enemies in a 4 block " +
                "radius around you are knocked up " +
                "and take 15 + (1 x lvl) physical " +
                "damage every second.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}
