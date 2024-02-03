package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the praetorium armor item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumArmorPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final double maxHealthPercent;
    private final long cooldown;

    public PraetoriumArmorPerk() {
        super("praetorium-armor");

        this.lastTimeUsed = new HashMap<>();

        this.maxHealthPercent = ((Number) this.config.get("max-health-percent")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("praetorium-armor-max-health", this, () -> this.maxHealthPercent));  //This is used in the configured lore
    }

    private void onRunicDamage(RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player) || !this.isActive(player)) {
            return;
        }

        Long lastActivated = this.lastTimeUsed.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (lastActivated != null && now - lastActivated < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(player.getUniqueId(), now);

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int amount = (int) (maxHealth * this.maxHealthPercent);

        event.setCancelled(true);
        RunicCore.getSpellAPI().shieldPlayer(player, player, amount);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onRunicDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onRunicDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        this.onRunicDamage(event);
    }
}