package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@SuppressWarnings("FieldCanBeLocal")
public class Ruination extends Spell implements HealingSpell, MagicDamageSpell {
    private static final int BASE_DURATION = 2;
    private static final int DAMAGE = 20;
    private static final int HEAL = 30;
    private static final int MAX_DIST = 2;
    private static final int RADIUS = 3;
    private static final double BEAM_WIDTH = 2;
    private static final double DAMAGE_PER_LEVEL = 1.0D;
    private static final double HEALING_PER_LEVEL = 0.75;
    private static final double PERCENT = 2.5;

    public Ruination() {
        super("Ruination",
                "For the next " + BASE_DURATION + "s, a " + RADIUS + " block radius around you " +
                        "becomes a realm of death! Enemies within the field " +
                        "take (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage per second! " +
                        "Additionally, every time this spell deals damage to an enemy, " +
                        "you heal✦ for (" + HEAL + " + &f" + HEALING_PER_LEVEL + "x&7 lvl) health and " +
                        "extend the duration of this spell by 1s, " +
                        "up to a max of 6s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 20, 30);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public int getHeal() {
        return HEAL;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }
}

