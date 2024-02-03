package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that models the praetorium archer item perk
 *
 * @author BoBoBalloon
 */
public class PraetoriumArcherWeaponPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final Map<UUID, Long> affected;
    private final double attackDamageConstant;
    private final double attackDamageMultiplier;
    private final double reducedSpeedPercent;
    private final long cooldown;

    public PraetoriumArcherWeaponPerk() {
        super("praetorium-archer-weapon");

        this.lastTimeUsed = new HashMap<>();
        this.affected = new ConcurrentHashMap<>();

        this.attackDamageConstant = ((Number) this.config.get("attack-constant")).doubleValue();
        this.attackDamageMultiplier = ((Number) this.config.get("attack-multiplier")).doubleValue();
        this.reducedSpeedPercent = ((Number) this.config.get("reduced-speed-percent")).doubleValue();
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

        AttributeInstance attribute = event.getVictim().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        if (attribute == null) {
            return;
        }

        this.affected.put(event.getVictim().getUniqueId(), now);

        int amount = RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), Stat.STRENGTH.getIdentifier());

        AttributeModifier modifier = new AttributeModifier("praetorium-archer-weapon", attribute.getBaseValue() * this.reducedSpeedPercent * -1, AttributeModifier.Operation.ADD_NUMBER);
        attribute.addModifier(modifier);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (System.currentTimeMillis() - now >= this.cooldown || !this.affected.containsKey(event.getVictim().getUniqueId()) || event.getVictim().isDead() || (event.getVictim() instanceof Player player && !player.isOnline())) {
                this.affected.remove(event.getVictim().getUniqueId());
                attribute.removeModifier(modifier);
                task.cancel();
                return;
            }

            DamageUtil.damageEntityPhysical((int) (amount * this.attackDamageMultiplier + this.attackDamageConstant), event.getVictim(), event.getPlayer(), false, false, false);
            event.getVictim().getWorld().spawnParticle(Particle.WHITE_ASH, event.getVictim().getLocation(), 30, 2 * Math.random(), 2 * Math.random(), 2 * Math.random());
        }, 0, 20);
    }
}
