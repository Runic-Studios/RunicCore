package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;

public class Manashield extends Spell implements ShieldingSpell {

    private static final int DAMAGE_AMOUNT = 10;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final double FIREBALL_SPEED = 2;
    private SmallFireball fireball;

    public Manashield() {
        super("Manashield",
                "You instantly shield yourself and " +
                        "up to 3 allies within 3 blocks for " +
                        "200 health! Whenever a shield breaks " +
                        "that was applied by this skill, " +
                        "you regain 20 mana.",
                ChatColor.WHITE, CharacterClass.MAGE, 4, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

    @Override
    public int getShield() {
        return 0;
    }

    @Override
    public double getShieldingPerLevel() {
        return 0;
    }


}

