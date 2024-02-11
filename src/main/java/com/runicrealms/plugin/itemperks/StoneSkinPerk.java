package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class StoneSkinPerk extends ItemPerkHandler {
    private final double incomingDamagePercentReduction;
    private final double outgoingDamagePercentReduction;

    public StoneSkinPerk() {
        super("stoneskin");

        this.incomingDamagePercentReduction = ((Number) this.config.get("incoming-damage-percent-reduction")).doubleValue();
        this.outgoingDamagePercentReduction = ((Number) this.config.get("outgoing-damage-percent-reduction")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("stoneskin-incoming-damage-percent-reduction", this, () -> this.incomingDamagePercentReduction));  //This is used in the configured lore
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("stoneskin-outgoing-damage-percent-reduction", this, () -> this.outgoingDamagePercentReduction));
    }

    private void reduce(@NotNull RunicDamageEvent event, @NotNull Player player, boolean incoming) {
        if (!this.isActive(player)) {
            return;
        }

        int stacks = this.getCurrentStacks(player);
        if (stacks == 0) {
            return;
        }

        double ratio = incoming ? this.incomingDamagePercentReduction : this.outgoingDamagePercentReduction;

        double damage = event.getAmount() * (1 - ratio * stacks);
        event.setAmount((int) damage);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.getVictim() instanceof Player) {
            return;
        }

        this.reduce(event, event.getPlayer(), false);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        if (event.getVictim() instanceof Player) {
            return;
        }

        this.reduce(event, event.getPlayer(), false);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) {
            return;
        }

        this.reduce(event, player, true);
    }
}
