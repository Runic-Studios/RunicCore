package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.api.event.StatusEffectEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * A class that models the magicnullification item perk
 *
 * @author BoBoBalloon
 */
public class MagicNullificationPerk extends ItemPerkHandler {
    public MagicNullificationPerk() {
        super("magicnullification");
    }

    @EventHandler(ignoreCancelled = true)
    private void onStatusEffect(StatusEffectEvent event) {
        if (!(event.getLivingEntity() instanceof Player player) || !this.isActive(player)) {
            return;
        }

        RunicStatusEffect effect = event.getRunicStatusEffect();

        if (effect == RunicStatusEffect.SLOW_I || effect == RunicStatusEffect.SLOW_II || effect == RunicStatusEffect.SLOW_III) {
            event.setCancelled(true);
        }
    }
}
