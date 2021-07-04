package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.WeaponDamageSpell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/*
Applies the per-level spell scaling for damage and healing before any other calculations are made.
 */
public class SpellScalingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellHeal(SpellHealEvent e) {
        if (e.getSpell() == null) return;
        if (!(e.getSpell() instanceof HealingSpell)) return;
        e.setAmount((int) (e.getAmount() + (((HealingSpell) e.getSpell()).getHealingPerLevel() * e.getPlayer().getLevel())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellDamage(SpellDamageEvent e) {
        if (e.getSpell() == null) return;
        if (!(e.getSpell() instanceof MagicDamageSpell)) return;
        e.setAmount((int) (e.getAmount() + (((MagicDamageSpell) e.getSpell()).getDamagePerLevel() * e.getPlayer().getLevel())));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (e.getSpell() == null) return;
        if (!(e.getSpell() instanceof WeaponDamageSpell)) return;
        e.setAmount((int) (e.getAmount() + (((WeaponDamageSpell) e.getSpell()).getDamagePerLevel() * e.getPlayer().getLevel())));
    }
}
