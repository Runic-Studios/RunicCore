package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.event.EventHandler;

/**
 * A class that models the manawell item perk
 *
 * @author BoBoBalloon
 */
public class ManawellPerk extends ItemPerkHandler {
    private final double spellDamageIncreasePercent;
    private final double perMissingManaPercent;

    public ManawellPerk() {
        super("manawell");

        this.spellDamageIncreasePercent = ((Number) this.config.get("spell-damage-per-missing-mana-percent")).doubleValue();
        this.perMissingManaPercent = ((Number) this.config.get("per-missing-mana-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("manawell-spell-damage-per-missing-mana-percent", this, () -> this.spellDamageIncreasePercent));
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!this.isActive(event.getPlayer())) {
            return;
        }

        double maxMana = ManaListener.calculateMaxMana(event.getPlayer());
        double current = RunicCore.getRegenManager().getCurrentManaList().get(event.getPlayer().getUniqueId()) / maxMana;
        int stacks = (int) ((1 - current) / this.perMissingManaPercent);

        event.setAmount(event.getAmount() + (int) (event.getAmount() * stacks * this.spellDamageIncreasePercent));
    }
}
