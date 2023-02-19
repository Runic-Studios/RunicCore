package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Applies the per-level spell scaling for damage and healing before any other calculations are made.
 */
public class SpellScalingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof PhysicalDamageSpell)) return;
        event.setAmount((int) (event.getAmount() + (((PhysicalDamageSpell) event.getSpell()).getDamagePerLevel() * event.getPlayer().getLevel())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof MagicDamageSpell)) return;
        event.setAmount((int) (event.getAmount() + (((MagicDamageSpell) event.getSpell()).getDamagePerLevel() * event.getPlayer().getLevel())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellHeal(SpellHealEvent event) {
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof HealingSpell)) return;
        event.setAmount((int) (event.getAmount() + (((HealingSpell) event.getSpell()).getHealingPerLevel() * event.getPlayer().getLevel())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellShield(SpellShieldEvent event) {
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof ShieldingSpell)) return;
        event.setAmount((int) (event.getAmount() + (((ShieldingSpell) event.getSpell()).getShieldingPerLevel() * event.getPlayer().getLevel())));
    }
}
