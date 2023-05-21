package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class Condemn extends Spell {

    public Condemn() {
        super("Condemn", CharacterClass.ROGUE);
        this.setDescription("?");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}
