package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.api.event.StatusEffectEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * New passive 1 for berserker
 *
 * @author BoBoBalloon
 */
public class Rupture extends Spell {
    private final Set<UUID> nextCrit;

    public Rupture() {
        super("Rupture", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("After successfully applying a bleed, your next basic attack is a critical hit! " +
                "If your enemy is bleeding, reset the duration of their bleed.");
        this.nextCrit = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !this.nextCrit.remove(event.getPlayer().getUniqueId()) || !event.isBasicAttack()) {
            return;
        }

        event.setCritical(true);

        if (this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.BLEED)) {
            this.addStatusEffect(event.getVictim(), RunicStatusEffect.BLEED, 6, false, event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true) //the effect apply is on high priority, this is right before it
    private void onStatusEffect(StatusEffectEvent event) {
        if (event.getRunicStatusEffect() != RunicStatusEffect.BLEED || event.getApplier() == null || !this.hasPassive(event.getApplier().getUniqueId(), this.getName()) || this.nextCrit.contains(event.getApplier().getUniqueId())) {
            return;
        }
        
        this.nextCrit.add(event.getApplier().getUniqueId());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.nextCrit.remove(event.getPlayer().getUniqueId());
    }
}

