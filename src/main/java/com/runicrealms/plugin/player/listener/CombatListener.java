package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnterCombatEvent;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.player.CombatType;
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
        EnterCombatEvent.tagPlayerAndPartyInCombat(event.getPlayer(), event.getCombatType());
    }

    @EventHandler
    public void onLeaveCombat(LeaveCombatEvent event) {
        RunicCore.getCombatAPI().leaveCombat(event.getPlayer().getUniqueId());
        RunicCore.getSpellAPI().setCooldown(event.getPlayer(), "Combat", 0);
        event.getPlayer().sendMessage(ChatColor.GREEN + "You have left combat!");
    }

    /**
     * Here the attacker is the MOB (no victim)
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player player)) return; // only listen when a player takes damage
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(player, CombatType.MOB);
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
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(event.getPlayer(), CombatType.MOB);
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
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(event.getPlayer(), CombatType.MOB);
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
    }
}
