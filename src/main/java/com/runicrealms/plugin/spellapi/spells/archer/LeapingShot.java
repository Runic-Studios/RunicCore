package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class LeapingShot extends Spell {

    public LeapingShot() {
        super("Leaping Shot", CharacterClass.ARCHER);
        this.setDescription("?");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
    }

}
