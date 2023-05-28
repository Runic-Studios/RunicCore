package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class BoulderToss extends Spell {

    public BoulderToss() {
        super("Boulder Toss", CharacterClass.WARRIOR);
        this.setDescription("You dig a massive boulder up from the earth " +
                "and throw it in the target direction! " +
                "Enemies within 2 blocks of impact take " +
                "30 + (1.25 x lvl) physical damage, " +
                "are stunned for 0.5s, " +
                "and are slowed for 1s afterwards!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}
