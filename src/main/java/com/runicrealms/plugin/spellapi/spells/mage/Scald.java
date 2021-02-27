package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;

/**
 * Logic for hit found in Fireball.
 */
public class Scald extends Spell {

    private static final double DAMAGE_PERCENT = .25;
    private static final int RADIUS = 4;

    public Scald() {
        super ("Scald",
                "Your &aFireball &7spell now deals, " +
                        (int) (DAMAGE_PERCENT * 100) + "% spellʔ damage to enemies within " +
                        RADIUS + " blocks!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    public static double getDamagePercent() {
        return DAMAGE_PERCENT;
    }

    public static int getRadius() {
        return RADIUS;
    }
}

