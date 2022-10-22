package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    @EventHandler
    public void onLeaveCombat(LeaveCombatEvent e) {
        RunicCore.getCombatManager().getPlayersInCombat().remove(e.getPlayer().getUniqueId());
        e.getPlayer().sendMessage(ChatColor.GREEN + "You have left combat!");
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
    public void onSpellDamage(MagicDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(e.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }

    /**
     * PhysicalDamageEvent can only be triggered by players, not mobs
     * If the victim is a Player, we let RunicPvP handle it
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (e.isCancelled()) return;
        if (e.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(e.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }
}
