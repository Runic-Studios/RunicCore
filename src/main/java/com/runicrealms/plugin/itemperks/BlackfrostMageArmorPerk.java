package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * A class that models the spellguardssurge item perk
 *
 * @author BoBoBalloon
 */
public class BlackfrostMageArmorPerk extends ItemPerkHandler {
    private final double damagePercent;

    public BlackfrostMageArmorPerk() {
        super("blackfrost-mage-armor");

        this.damagePercent = ((Number) this.config.get("damage-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("blackfrost-mage-armor-damage-percent", this, () -> this.damagePercent));
    }

    private void onRunicDamage(RunicDamageEvent event, Player damager) {
        if (!this.isActive(damager) || !RunicCore.getSpellAPI().isShielded(damager.getUniqueId())) {
            return;
        }

        event.setAmount((int) (event.getAmount() * (1 + this.damagePercent * this.getCurrentStacks(damager))));
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onRunicDamage(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onRunicDamage(event, event.getPlayer());
    }
}