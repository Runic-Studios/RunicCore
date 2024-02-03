package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that models the praetorium mage item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumMageWeaponPerk extends ItemPerkHandler {
    private final double cooldownReductionPercent;

    public PraetoriumMageWeaponPerk() {
        super("praetorium-mage-weapon");

        this.cooldownReductionPercent = ((Number) this.config.get("cooldown-reduction-percent")).doubleValue();
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack() || !this.isActive(event.getPlayer())) {
            return;
        }

        ConcurrentHashMap.KeySetView<Spell, Long> cooldowns = RunicCore.getSpellAPI().getSpellsOnCooldown(event.getPlayer().getUniqueId());

        if (cooldowns == null) {
            return;
        }

        for (Spell spell : cooldowns) {
            double cooldown = RunicCore.getSpellAPI().getUserCooldown(event.getPlayer(), spell);
            RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), spell, cooldown * this.cooldownReductionPercent);
        }
    }
}