package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class WardingGlyph extends Spell {

    public WardingGlyph() {
        super("Warding Glyph", CharacterClass.ROGUE);
        this.setDescription("?");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}