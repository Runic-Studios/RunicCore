package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.ManaRegenEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A class that models the aegis item perk
 *
 * @author BoBoBalloon
 */
public class GluttonyPerk extends ItemPerkHandler implements Listener {
    private final double manaRegenPercent;
    private final double manaRegenCutoff;
    private final int extraDamagePerMissingHealth;
    private final double perMissingHealthPercent;
    private final int extraDamageIfLowHP;
    private final double lowHPCutoff;

    public GluttonyPerk() {
        super("gluttony");

        this.manaRegenPercent = ((Number) this.config.get("mana-regen-percent")).doubleValue();
        this.manaRegenCutoff = ((Number) this.config.get("mana-regen-cutoff")).doubleValue();
        this.extraDamagePerMissingHealth = ((Number) this.config.get("extra-damage-per-missing-health")).intValue();
        this.perMissingHealthPercent = ((Number) this.config.get("per-missing-health-percent")).doubleValue();
        this.extraDamageIfLowHP = ((Number) this.config.get("extra-damage-low-hp")).intValue();
        this.lowHPCutoff = ((Number) this.config.get("low-hp-cutoff")).doubleValue();
    }

    @EventHandler(ignoreCancelled = true)
    private void onManaRegen(ManaRegenEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (event.getPlayer().getHealth() / maxHealth < this.manaRegenCutoff) {
            return;
        }

        double amount = event.getAmount() * (1 + this.manaRegenPercent);
        event.setAmount((int) amount);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack() || !this.isActive(event.getPlayer())) {
            return;
        }

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double current = event.getPlayer().getHealth() / maxHealth;
        int stacks = (int) ((1 - current) / this.perMissingHealthPercent);

        int bonus = current <= this.lowHPCutoff ? this.extraDamageIfLowHP : 0;

        event.setAmount(event.getAmount() + (stacks * this.extraDamagePerMissingHealth) + bonus);
    }
}
