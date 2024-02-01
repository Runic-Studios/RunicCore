package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.warrior.BleedEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * New passive 1 for berserker
 *
 * @author BoBoBalloon
 */
public class Rupture extends Spell {
    private final Set<UUID> nextCriticalSet;

    public Rupture() {
        super("Rupture", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("After successfully applying &cbleed&7, your next basic attack will critically strike! " +
                "If your enemy is &cbleeding&7, reset the duration of their &cbleed&7.");
        this.nextCriticalSet = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.nextCriticalSet.contains(event.getPlayer().getUniqueId())) return;
        if (!event.isBasicAttack()) return;
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;

        event.setCritical(true);
        this.nextCriticalSet.remove(event.getPlayer().getUniqueId());

        Optional<SpellEffect> bleedEffect = this.getSpellEffect(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId(), SpellEffectType.BLEED);
        if (bleedEffect.isEmpty()) return;
        ((BleedEffect) bleedEffect.get()).refreshStacks();
    }

    @EventHandler(ignoreCancelled = true)
    private void onStatusEffect(SpellEffectEvent event) {
        if (this.nextCriticalSet.contains(event.getSpellEffect().getCaster().getUniqueId())) return;
        if (event.getSpellEffect().getEffectType() != SpellEffectType.BLEED) return;
        if (!this.hasPassive(event.getSpellEffect().getCaster().getUniqueId(), this.getName())) return;

        this.nextCriticalSet.add(event.getSpellEffect().getCaster().getUniqueId());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.nextCriticalSet.remove(event.getPlayer().getUniqueId());
    }
}

