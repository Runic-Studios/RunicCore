package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class Salvation extends Spell {

    public Salvation() {
        super("Salvation", CharacterClass.WARRIOR);
        this.setDescription("?");
    }


    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // ?
    }
}
