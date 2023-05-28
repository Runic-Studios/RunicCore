package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

public class Shockwave extends Spell {

    public Shockwave() {
        super("Shockwave", CharacterClass.WARRIOR);
        this.setDescription("You smash your foot down, sending out a " +
                "shockwave in a line in front of you, knocking up " +
                "all enemies hit and dealing 30 + (0.75 x lvl) physical damage! " +
                "Mobs hit by Shockwave are taunted, causing them to attack you.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

}
