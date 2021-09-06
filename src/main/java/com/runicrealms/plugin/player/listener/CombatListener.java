package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CombatListener implements Listener {

    @EventHandler
    public void onEnterCombat(EnterCombatEvent e) {
        if (e.isCancelled()) return;
        EnterCombatEvent.tagPlayerAndPartyInCombat(e.getPlayer());
    }

    /**
     * Here the attacker is the MOB (no victim)
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onMobDamage(MobDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getVictim() instanceof Player)) return; // only listen when a player takes damage
        Player player = (Player) e.getVictim();
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(player);
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }

    /**
     * SpellDamageEvent can only be triggered by players, not mobs
     * If the victim is a Player, we let RunicPvP handle it
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onSpellDamage(SpellDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(e.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }

    /**
     * WeaponDamageEvent can only be triggered by players, not mobs
     * If the victim is a Player, we let RunicPvP handle it
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(e.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }
}
