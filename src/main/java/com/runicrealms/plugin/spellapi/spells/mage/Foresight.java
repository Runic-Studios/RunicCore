package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.UUID;

public class Foresight extends Spell {

    private static final int DURATION = 2;
    private static final double PERCENT_REDUCTION = .5;
    private static final HashSet<UUID> doomers = new HashSet<>();

    public Foresight() {
        super("Foresight",
                "After casting your &aBlink &7spell, " +
                        "you gain " + (int) (PERCENT_REDUCTION * 100) + "% " +
                        "damage reduction for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlinkCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Blink)) return;
        doomers.add(event.getCaster().getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                () -> doomers.remove(event.getCaster().getUniqueId()), DURATION * 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }
}

