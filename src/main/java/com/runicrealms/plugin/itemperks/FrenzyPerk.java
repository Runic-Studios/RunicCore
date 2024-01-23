package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.event.EventHandler;

public class FrenzyPerk extends ItemPerkHandler {

    private final double attackSpeedIncrease;

    public FrenzyPerk() {
        super("frenzy");

        this.attackSpeedIncrease = ((Number) this.config.get("attack-speed-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("frenzy-atkspd", this, () -> this.attackSpeedIncrease));  //This is used in the configured lore
    }

    @EventHandler(ignoreCancelled = true)
    private void onBasicAttack(BasicAttackEvent event) {
        if (!isActive(event.getPlayer())) return;

        double percentReduce = attackSpeedIncrease * getCurrentStacks(event.getPlayer());
        double ticksToReduce = event.getOriginalCooldownTicks() * percentReduce;
        event.setCooldownTicks(Math.max(event.getUnroundedCooldownTicks() - ticksToReduce, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS));
    }

}