package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class SilverBolt extends Spell {

    public SilverBolt() {
        super("Silver Bolt", CharacterClass.ROGUE);
        this.setDescription("?");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}

