package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FrenzyPerk extends ItemPerkHandler implements Listener {

    private final Set<UUID> active;
    private final double attackSpeedIncrease;

    public FrenzyPerk() {
        super("frenzy");

        this.active = new HashSet<>();

        this.attackSpeedIncrease = ((Number) this.config.get("attack-speed-percent")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("frenzy-atkspd", this, () -> this.attackSpeedIncrease));  //This is used in the configured lore
    }

    @Override
    public void onChange(Player player, int stacks) {
        if (stacks > 0) {
            this.active.add(player.getUniqueId());
        } else {
            this.active.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBasicAttack(BasicAttackEvent event) {
        if (!this.active.contains(event.getPlayer().getUniqueId())) return;

        double percentReduce = attackSpeedIncrease * getCurrentStacks(event.getPlayer());
        double ticksToReduce = event.getOriginalCooldownTicks() * percentReduce;
        event.setCooldownTicks(Math.max(event.getUnroundedCooldownTicks() - ticksToReduce, BasicAttackEvent.MINIMUM_COOLDOWN_TICKS));
    }

}