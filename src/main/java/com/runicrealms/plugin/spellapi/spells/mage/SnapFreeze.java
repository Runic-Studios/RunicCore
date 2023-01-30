package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SnapFreeze extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMOUNT = 20;
    private static final int DAMAGE_PER_LEVEL = 1;
    private static final double DURATION = 0.5;

    public SnapFreeze() {
        super("Snap Freeze",
                "You cast a wave of frost in a forward line! " +
                        "Enemies hit by the spell take (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage and are stunned for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 4, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }


}

