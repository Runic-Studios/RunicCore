package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that models the praetorium rogue item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumRogueWeaponPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final Map<UUID, Long> affected;
    private final double attackDamageConstant;
    private final double attackDamageMultiplier;
    private final double reducedHealingPercent;
    private final long cooldown;

    public PraetoriumRogueWeaponPerk() {
        super("praetorium-rogue-weapon");

        this.lastTimeUsed = new HashMap<>();
        this.affected = new ConcurrentHashMap<>();

        this.attackDamageConstant = ((Number) this.config.get("attack-constant")).doubleValue();
        this.attackDamageMultiplier = ((Number) this.config.get("attack-multiplier")).doubleValue();
        this.reducedHealingPercent = ((Number) this.config.get("reduced-healing-percent")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isBasicAttack() || !this.isActive(event.getPlayer())) {
            return;
        }

        Long lastActivated = this.lastTimeUsed.get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        if (lastActivated != null && now - lastActivated < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(event.getPlayer().getUniqueId(), now);
        this.affected.put(event.getVictim().getUniqueId(), now);

        int amount = RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), Stat.STRENGTH.getIdentifier());

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (System.currentTimeMillis() - now >= this.cooldown || !this.affected.containsKey(event.getVictim().getUniqueId())) {
                this.affected.remove(event.getVictim().getUniqueId());
                task.cancel();
                return;
            }

            DamageUtil.damageEntityPhysical((int) (amount * this.attackDamageMultiplier + this.attackDamageConstant), event.getVictim(), event.getPlayer(), false, false, false);
            event.getVictim().getWorld().spawnParticle(Particle.SPELL_WITCH, event.getVictim().getLocation(), 30, 2 * Math.random(), 2 * Math.random(), 2 * Math.random());
        }, 0, 20);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    //this goes after the normal stat bonuses have been applied
    private void onSpellHeal(SpellHealEvent event) {
        if (!this.affected.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        double amount = event.getAmount() * (1 - this.reducedHealingPercent);

        event.setAmount((int) amount);
    }
}
