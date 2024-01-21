package com.runicrealms.plugin.itemperks;

import com.google.common.collect.ImmutableList;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A class that models the gluttony item perk
 *
 * @author BoBoBalloon
 */
public class WintersEdge2023Perk extends ItemPerkHandler {
    private final double damagePercent;

    private static final ImmutableList<String> MOB_IDS = ImmutableList.copyOf(new String[]{"winter-2023-elite-20", "winter-2023-elite-40", "winter-2023-elite-60", "winter-2023-world-boss"});

    public WintersEdge2023Perk() {
        super("wintersedge2023");

        this.damagePercent = ((Number) this.config.get("damage-percent-per-stack")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("wintersedge2023-damage-percent", this, () -> this.damagePercent));  //This is used in the configured lore
    }

    private void onDamage(@NotNull RunicDamageEvent event, @NotNull Player player) {
        if (!this.isActive(player)) {
            return;
        }

        ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(event.getVictim());

        if (mob == null || !MOB_IDS.contains(mob.getType().getInternalName())) {
            return;
        }

        double bonus = event.getAmount() * this.damagePercent * this.getCurrentStacks(player);

        event.setAmount(event.getAmount() + (int) bonus);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onDamage(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onDamage(event, event.getPlayer());
    }
}