package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.perk.DynamicItemPerkPercentStatPlaceholder;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.spellapi.event.SpellHealEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the overheal item perk
 *
 * @author BoBoBalloon
 */
public class BlackfrostClericArmorPerk extends ItemPerkHandler {
    private static final long COOLDOWN = 100; //.1 seconds
    private final Map<UUID, Long> limiter;
    private final double damagePercent;
    private final double range;

    public BlackfrostClericArmorPerk() {
        super("blackfrost-cleric-armor");

        this.limiter = new HashMap<>();

        this.damagePercent = ((Number) this.config.get("damage-percent")).doubleValue();
        this.range = ((Number) this.config.get("range")).doubleValue();

        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new DynamicItemPerkPercentStatPlaceholder("blackfrost-cleric-armor-damage-percent", this, () -> this.damagePercent));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    //high since highest sets it to max health if it is over
    private void onSpellHeal(SpellHealEvent event) {
        Long lastTriggered = this.limiter.get(event.getPlayer().getUniqueId());
        if (lastTriggered != null && System.currentTimeMillis() - lastTriggered < COOLDOWN) {
            return;
        }

        if (!this.isActive(event.getPlayer()) || !(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (instance == null) {
            return;
        }

        double maxHealth = instance.getValue();
        double overheal = event.getAmount() + entity.getHealth() - maxHealth;

        if (overheal <= 0) {
            return;
        }

        int amount = (int) Math.round(overheal * this.damagePercent * this.getCurrentStacks(event.getPlayer()));

        for (Entity nearby : event.getPlayer().getNearbyEntities(this.range, this.range, this.range)) {
            if (!(nearby instanceof LivingEntity target)) {
                continue;
            }

            EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(event.getPlayer(), target);
            Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);

            if (enemyVerifyEvent.isCancelled()) {
                continue;
            }

            DamageUtil.damageEntitySpell(amount, target, event.getPlayer(), false);
        }

        this.limiter.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}