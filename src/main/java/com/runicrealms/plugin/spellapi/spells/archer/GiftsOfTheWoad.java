package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class GiftsOfTheWoad extends Spell {

    private static final double DURATION = 0.5;

    public GiftsOfTheWoad() {
        super("Gifts of the Woad",
                "Each time you land a ranged attack on an enemy, " +
                        "reduce the cooldown of your &aRemedy &7and &aWild Growth &7spells by " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(RunicCore.getSpellAPI().isOnCooldown(event.getPlayer(), "Remedy")
                || RunicCore.getSpellAPI().isOnCooldown(event.getPlayer(), "Wild Growth"))) return;
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Remedy", DURATION);
        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Wild Growth", DURATION);
    }
}

