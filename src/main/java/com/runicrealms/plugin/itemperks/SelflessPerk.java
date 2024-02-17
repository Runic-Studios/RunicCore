package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.event.SpellHealEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * A class that models the selfless item perk
 *
 * @author BoBoBalloon
 */
public class SelflessPerk extends ItemPerkHandler {
    private final double incomingHealingReduction;
    private final double outgoingHealingIncrease;

    public SelflessPerk() {
        super("selfless");

        this.incomingHealingReduction = ((Number) this.config.get("incoming-healing-percent-reduction")).doubleValue();
        this.outgoingHealingIncrease = ((Number) this.config.get("outgoing-healing-percent-increase")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("selfless-incoming-healing-percent-reduction", this, () -> this.incomingHealingReduction));  //This is used in the configured lore
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("selfless-outgoing-healing-percent-increase", this, () -> this.outgoingHealingIncrease));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onSpellHeal(SpellHealEvent event) {
        this.incoming(event);
        this.outgoing(event);
    }

    private void incoming(SpellHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || !this.isActive(player)) {
            return;
        }

        int stacks = this.getCurrentStacks(player);
        if (stacks == 0) {
            return;
        }

        double amount = event.getAmount() * (1 - this.incomingHealingReduction * stacks);

        event.setAmount((int) amount);
    }

    private void outgoing(SpellHealEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        int stacks = this.getCurrentStacks(event.getPlayer());
        if (stacks == 0) {
            return;
        }

        double amount = event.getAmount() * (1 + this.outgoingHealingIncrease * stacks);

        event.setAmount((int) amount);
    }
}