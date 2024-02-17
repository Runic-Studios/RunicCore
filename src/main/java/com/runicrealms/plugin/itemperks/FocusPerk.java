package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * A class that models the focus item perk
 *
 * @author BoBoBalloon
 */
public class FocusPerk extends ItemPerkHandler {
    private final double increaseDamagePercent;

    public FocusPerk() {
        super("focus");

        this.increaseDamagePercent = ((Number) this.config.get("increase-damage-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("focus-increase-damage-percent", this, () -> this.increaseDamagePercent));
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.effect(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.effect(event, event.getPlayer());
    }

    private void effect(RunicDamageEvent event, Player player) {
        if (!this.isActive(player)) {
            return;
        }

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (player.getHealth() != maxHealth) {
            return;
        }

        double amount = event.getAmount() * (1 + this.increaseDamagePercent * this.getCurrentStacks(player));

        event.setAmount((int) amount);
    }
}
