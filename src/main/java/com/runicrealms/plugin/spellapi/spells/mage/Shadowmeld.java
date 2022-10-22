package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

public class Shadowmeld extends Spell {

    private static final int DURATION = 2;
    private static final double PERCENT_REDUCTION = .75;
    private static final HashSet<UUID> doomers = new HashSet<>();

    public Shadowmeld() {
        super("Shadowmeld",
                "After casting your &aBlink &7spell, " +
                        "you gain " + (int) (PERCENT_REDUCTION * 100) + "% " +
                        "damage reduction for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (!doomers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT_REDUCTION)));
    }

    @EventHandler
    public void onBlinkCast(SpellCastEvent e) {
        if (!hasPassive(e.getCaster().getUniqueId(), this.getName())) return;
        if (!(e.getSpell() instanceof Blink)) return;
        doomers.add(e.getCaster().getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                () -> doomers.remove(e.getCaster().getUniqueId()), DURATION * 20L);
    }

    public static int getDuration() {
        return DURATION;
    }

    public static HashSet<UUID> getDoomers() {
        return doomers;
    }
}

