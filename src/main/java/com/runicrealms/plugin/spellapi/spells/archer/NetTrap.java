package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class NetTrap extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 4;
    private static final double DAMAGE_PER_LEVEL = 0.4;
    private final HashMap<Arrow, UUID> bArrows;

    public NetTrap() {
        super("Net Trap",
                "?",
                ChatColor.WHITE, CharacterClass.ARCHER, 6, 10);
        this.bArrows = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

}
