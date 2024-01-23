package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class RavenousPerk extends ItemPerkHandler {
    private final double healthCutoff;
    private final double healthPercentRestored;

    public RavenousPerk() {
        super("ravenous");

        this.healthCutoff = ((Number) this.config.get("health-percent-threshold")).doubleValue();
        this.healthPercentRestored = ((Number) this.config.get("health-percent-per-stack")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("ravenous-health-restored", this, () -> this.healthPercentRestored));  //This is used in the configured lore
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!isActive(event.getPlayer())) return;

        int stacks = getCurrentStacks(event.getPlayer());
        if (stacks == 0) return;

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (event.getPlayer().getHealth() >= maxHealth * this.healthCutoff) {
            return;
        }

        double heal = this.healthPercentRestored * stacks * maxHealth;
        RunicCore.getSpellAPI().healPlayer(event.getPlayer(), event.getPlayer(), heal);
    }

}
