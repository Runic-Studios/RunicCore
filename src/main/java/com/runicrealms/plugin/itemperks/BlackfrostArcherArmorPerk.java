package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * A class that models the opportunist item perk
 *
 * @author BoBoBalloon
 */
public class BlackfrostArcherArmorPerk extends ItemPerkHandler {
    private final double damagePercent;

    public BlackfrostArcherArmorPerk() {
        super("blackfrost-archer-armor");

        this.damagePercent = ((Number) this.config.get("damage-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("blackfrost-archer-armor-damage-percent", this, () -> this.damagePercent));
    }

    private void onRunicDamage(RunicDamageEvent event, Player damager) {
        LivingEntity entity = event.getVictim();
        if (!this.isActive(damager) || entity.isOnGround() || !(RunicCore.getStatusEffectAPI().hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.ROOT) || RunicCore.getStatusEffectAPI().hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.STUN))) {
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