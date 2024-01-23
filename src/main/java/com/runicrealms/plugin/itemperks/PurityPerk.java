package com.runicrealms.plugin.itemperks;

import com.google.common.collect.ImmutableSet;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spells.Potion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * A class that models the purity item perk
 *
 * @author BoBoBalloon
 */
public class PurityPerk extends ItemPerkHandler {
    private final double healReductionPercent;

    private static final ImmutableSet<RunicStatusEffect> EFFECTS = ImmutableSet.of(RunicStatusEffect.ROOT, RunicStatusEffect.STUN, RunicStatusEffect.SLOW_I, RunicStatusEffect.SLOW_II, RunicStatusEffect.SLOW_III);

    public PurityPerk() {
        super("purity");

        this.healReductionPercent = ((Number) this.config.get("heal-reduced-percent")).doubleValue();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) //this goes after normal spell execution
    private void onSpellCast(SpellCastEvent event) {
        if (!(event.getSpell() instanceof Potion) || !this.isActive(event.getCaster())) {
            return;
        }

        for (RunicStatusEffect effect : EFFECTS) {
            RunicCore.getStatusEffectAPI().removeStatusEffect(event.getCaster().getUniqueId(), effect);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    //this goes after the normal stat bonuses have been applied
    private void onSpellHeal(SpellHealEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        double amount = event.getAmount() * (1 - this.healReductionPercent);

        event.setAmount((int) amount);
    }
}
