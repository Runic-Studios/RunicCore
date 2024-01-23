package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the undying item perk
 *
 * @author BoBoBalloon
 */
public class UndyingPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed; // do not remove the player from the cooldown map on quit as that could be an exploit in the making (we already know our players love to leave and rejoin quickly)
    private final double healthRestored;
    private final long cooldown;

    public UndyingPerk() {
        super("undying");

        this.lastTimeUsed = new HashMap<>();

        this.healthRestored = ((Number) this.config.get("health-percent-per-stack")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("undying-health-restored", this, () -> this.healthRestored));  //This is used in the configured lore
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) //death logic has a prioity of highest
    private void onRunicDeath(RunicDeathEvent event) {
        if (!this.isActive(event.getVictim())) {
            return;
        }

        Long lastUndying = this.lastTimeUsed.get(event.getVictim().getUniqueId());
        long now = System.currentTimeMillis();

        if (lastUndying != null && now - lastUndying < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(event.getVictim().getUniqueId(), now);
        event.setCancelled(true);

        double maxHealth = event.getVictim().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double restore = maxHealth * this.healthRestored * this.getCurrentStacks(event.getVictim());
        event.getVictim().setHealth(restore);

        event.getVictim().getWorld().spawnParticle(Particle.SCULK_SOUL, event.getLocation(), 100, 0, 0, 0, 5);
        event.getVictim().playSound(event.getLocation(), Sound.ITEM_TOTEM_USE, SoundCategory.AMBIENT, 1, 1);
    }

}
