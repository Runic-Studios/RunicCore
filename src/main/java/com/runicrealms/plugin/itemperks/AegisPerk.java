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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the aegis item perk
 *
 * @author BoBoBalloon
 */
public class AegisPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final double shieldPercent;
    private final long cooldown;

    public AegisPerk() {
        super("aegis");

        this.lastTimeUsed = new HashMap<>();

        this.shieldPercent = ((Number) this.config.get("shield-percent-per-stack")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("aegis-shield-percent-per-stack", this, () -> this.shieldPercent));  //This is used in the configured lore
    }

    private void onDamage(@NotNull RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player) || !this.isActive(player)) {
            return;
        }

        int stacks = this.getCurrentStacks(player);
        if (stacks == 0) {
            return;
        }

        Long lastActivated = this.lastTimeUsed.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (lastActivated != null && now - lastActivated < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(player.getUniqueId(), now);

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double shield = maxHealth * this.shieldPercent * stacks;
        RunicCore.getSpellAPI().shieldPlayer(player, player, shield);
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
