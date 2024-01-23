package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.listeners.ShieldListener;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.entity.Player;

/**
 * A class that models the fortitude item perk
 *
 * @author BoBoBalloon
 */
public class FortitudePerk extends ItemPerkHandler {
    private final double shieldCapacityPercentIncrease;

    public FortitudePerk() {
        super("fortitude");

        this.shieldCapacityPercentIncrease = ((Number) this.config.get("shield-capacity-percent-per-stack")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("fortitude-shield-capacity-percent-per-stack", this, () -> this.shieldCapacityPercentIncrease));  //This is used in the configured lore
    }

    @Override
    public void onChange(Player player, int stacks) {
        if (stacks > 0) {
            ShieldListener.getBonusCap().put(player.getUniqueId(), this.shieldCapacityPercentIncrease * stacks);
        } else {
            ShieldListener.getBonusCap().remove(player.getUniqueId());
        }
    }
}
