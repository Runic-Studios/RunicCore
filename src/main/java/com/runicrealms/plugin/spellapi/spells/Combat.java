package com.runicrealms.plugin.spellapi.spells;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnterCombatEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to display combat timer on hotbar
 */
public class Combat extends Spell {

    public Combat() {
        super("Combat", CharacterClass.ANY);
        this.setDisplayCastMessage(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // last
    public void onEnterCombat(EnterCombatEvent event) {
        if (this.isOnCooldown(event.getPlayer())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new SpellCastEvent(event.getPlayer(), this));
        this.resetCombatTimerDisplay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        resetCombatTimerDisplay(event.getPlayer());
        if (event.getVictim() instanceof Player victim)
            resetCombatTimerDisplay(victim);
    }

    /**
     * Updates the hotbar cooldown ui
     *
     * @param player to update combat timer for
     */
    private void resetCombatTimerDisplay(Player player) {
        if (!isOnCooldown(player)) return;
        UUID uuid = player.getUniqueId();
        ConcurrentHashMap.KeySetView<Spell, Long> spellsOnCooldown = RunicCore.getSpellAPI().getSpellsOnCooldown(uuid);
        if (spellsOnCooldown == null) return;
        double durationToReach = RunicCore.getCombatAPI().getCombatType(uuid).getCooldown();
        double durationToIncrease = durationToReach - RunicCore.getSpellAPI().getUserCooldown(player, this);
        if (durationToIncrease < 0) return; // Handles a race condition where current cooldown is higher than max
        RunicCore.getSpellAPI().increaseCooldown
                (
                        player,
                        this,
                        durationToIncrease
                );
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        resetCombatTimerDisplay(event.getPlayer());
        if (event.getVictim() instanceof Player victim)
            resetCombatTimerDisplay(victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player player)) return;
        resetCombatTimerDisplay(player);
    }

    @Override
    public double getCooldown() {
        return 1;
    }
}

