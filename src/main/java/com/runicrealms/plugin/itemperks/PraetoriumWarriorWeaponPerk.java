package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the praetorium warrior item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumWarriorWeaponPerk extends ItemPerkHandler {
    private final Map<UUID, Long> affected;
    private final double damageReceivedIncreasePercent;
    private final long duration;

    public PraetoriumWarriorWeaponPerk() {
        super("praetorium-warrior-weapon");

        this.affected = new HashMap<>();

        this.damageReceivedIncreasePercent = ((Number) this.config.get("damage-received-increase-percent")).doubleValue();
        this.duration = ((Number) this.config.get("duration")).longValue() * 1000; //convert seconds to milliseconds
    }

    private void onRunicDamage(RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player) || !this.isActive(player)) {
            return;
        }

        Long lastTime = this.affected.get(player.getUniqueId());
        if (lastTime == null || System.currentTimeMillis() - lastTime > this.duration) {
            this.affected.remove(player.getUniqueId());
            return;
        }

        event.setAmount((int) (event.getAmount() * (1 + this.damageReceivedIncreasePercent)));
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onRunicDamage(event);

        if (!event.isBasicAttack() || !this.isActive(event.getPlayer())) {
            return;
        }

        this.affected.put(event.getVictim().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onRunicDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEnvironmentDamage(EnvironmentDamageEvent event) {
        this.onRunicDamage(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMobDamage(MobDamageEvent event) {
        this.onRunicDamage(event);
    }
}
