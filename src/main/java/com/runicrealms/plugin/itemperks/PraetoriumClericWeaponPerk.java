package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the praetorium cleric item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumClericWeaponPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final double maxHealthPercent;
    private final long cooldown;

    public PraetoriumClericWeaponPerk() {
        super("praetorium-cleric-weapon");

        this.lastTimeUsed = new HashMap<>();

        this.maxHealthPercent = ((Number) this.config.get("max-health-percent")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack() || !this.isActive(event.getPlayer())) {
            return;
        }

        double maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int amount = (int) (maxHealth * this.maxHealthPercent);

        if (event.getPlayer().getHealth() <= amount) {
            return;
        }

        Long lastActivated = this.lastTimeUsed.get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        if (lastActivated != null && now - lastActivated < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(event.getPlayer().getUniqueId(), now);

        DamageUtil.damageEntityGeneric(amount, event.getPlayer(), false);
        event.setAmount(event.getAmount() + amount);
    }
}