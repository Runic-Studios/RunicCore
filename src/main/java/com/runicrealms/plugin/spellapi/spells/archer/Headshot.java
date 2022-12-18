package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.RangedDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Headshot extends Spell {

    private static final double PERCENT = 1.25;

    public Headshot() {
        super("Headshot",
                "Damaging an enemy with a headshot deals " + (int) (PERCENT * 100) + "% damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onHeadshot(RangedDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;

        // skip party members
        double arrowLocationY = event.getArrow().getLocation().getY();
        double victimLocationY = event.getVictim().getLocation().getY();

        boolean headshot = arrowLocationY - victimLocationY > 1.35d; // verify headshot using distance from Y
        if (!headshot) return;

        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
        event.getVictim().getWorld().spawnParticle(Particle.CRIT, event.getVictim().getEyeLocation(), 3, 0.25f, 0.25f, 0.25f);
        event.setAmount((int) (event.getAmount() * PERCENT));
    }

}

