package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("FieldCanBeLocal")
public class BindingShot extends Spell {

    public BindingShot() {
        super("Binding Shot",
                "?",
                ChatColor.WHITE, ClassEnum.ARCHER, 12, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        // ?
    }
}
