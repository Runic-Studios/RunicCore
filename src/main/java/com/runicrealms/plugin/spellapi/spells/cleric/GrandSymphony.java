package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

/**
 * New bard ultimate spell
 *
 * @author BoBoBalloon
 */
public class GrandSymphony extends Spell {
    public GrandSymphony() {
        super("Grand Symphony", CharacterClass.CLERIC);
        this.setDescription("");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }
}
