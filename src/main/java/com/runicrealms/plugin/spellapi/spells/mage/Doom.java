package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

/**
 * Logic for hit found in Shadow Bomb
 */
public class Doom extends Spell {

    private static final int DURATION = 2;
    private static final HashSet<UUID> doomers = new HashSet<>();

    public Doom() {
        super ("Doom",
                "After casting your &aBlink &7spell, " +
                        "your next &Shadow Bomb " +
                        "&7silences its target(s) for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster(), this.getName())) return;
        if (!(e.getSpell() instanceof Blink)) return;
        doomers.add(e.getCaster().getUniqueId());
    }

    public static int getDuration() {
        return DURATION;
    }

    public static HashSet<UUID> getDoomers() {
        return doomers;
    }
}

