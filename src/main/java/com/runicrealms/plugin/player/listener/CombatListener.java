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
    public void onEnterCombat(EnterCombatEvent event) {
        if (event.isCancelled()) return;
        EnterCombatEvent.tagPlayerAndPartyInCombat(event.getPlayer());
    }

    @EventHandler
    public void onLeaveCombat(LeaveCombatEvent event) {
        RunicCore.getCombatAPI().leaveCombat(event.getPlayer().getUniqueId());
        event.getPlayer().sendMessage(ChatColor.GREEN + "You have left combat!");
    }

    /**
     * Here the attacker is the MOB (no victim)
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return; // only listen when a player takes damage
        Player player = (Player) event.getVictim();
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(player);
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }

    /**
     * PhysicalDamageEvent can only be triggered by players, not mobs
     * If the victim is a Player, we let RunicPvP handle it
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(event.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }

    /**
     * SpellDamageEvent can only be triggered by players, not mobs
     * If the victim is a Player, we let RunicPvP handle it
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getVictim() instanceof Player) return; // handled in RunicPvP
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(event.getPlayer());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }
}
