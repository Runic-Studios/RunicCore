package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class StoneSkinPerk extends ItemPerkHandler {
    private final double damagePercentReduction;

    public StoneSkinPerk() {
        super("stoneskin");

        this.damagePercentReduction = ((Number) this.config.get("damage-percent-reduction")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("stoneskin-damage-percent-reduction", this, () -> this.damagePercentReduction));  //This is used in the configured lore

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> this.getActive()
                .stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(player -> RunicCore.getStatusEffectAPI().addStatusEffect(player, RunicStatusEffect.SLOW_II, 0.5, false)), 10, 0);
    }

    private void onDamage(@NotNull RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        if (!isActive(player)) return;

        int stacks = this.getCurrentStacks(player);
        if (stacks == 0) return;

        double damage = event.getAmount() * (1 - this.damagePercentReduction * stacks);
        event.setAmount((int) damage);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnvironmentDamage(EnvironmentDamageEvent event) {
        this.onDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        this.onDamage(event);
    }
}
