package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the infernal item perk
 *
 * @author BoBoBalloon
 */
public class InfernalPerk extends ItemPerkHandler {
    private final Map<UUID, Long> lastTimeUsed;
    private final double attackDamageConstant;
    private final double attackDamageMultiplier;
    private final double spellDamageConstant;
    private final double spellDamageMultiplier;
    private final long cooldown;

    private static final Stat ATTACK = Stat.STRENGTH;
    private static final Stat SPELL = Stat.INTELLIGENCE;

    public InfernalPerk() {
        super("infernal");

        this.lastTimeUsed = new HashMap<>();

        this.attackDamageConstant = ((Number) this.config.get("attack-constant")).doubleValue();
        this.attackDamageMultiplier = ((Number) this.config.get("attack-multiplier")).doubleValue();
        this.spellDamageConstant = ((Number) this.config.get("spell-constant")).doubleValue();
        this.spellDamageMultiplier = ((Number) this.config.get("spell-multiplier")).doubleValue();
        this.cooldown = ((Number) this.config.get("cooldown")).longValue() * 1000; //convert seconds to milliseconds
    }

    private void onDamage(@NotNull RunicDamageEvent event, Player player, boolean spell) {
        if (!this.isActive(player)) {
            return;
        }

        Long lastActivated = this.lastTimeUsed.get(player.getUniqueId());
        long now = System.currentTimeMillis();
        if (lastActivated != null && now - lastActivated < this.cooldown) {
            return;
        }

        this.lastTimeUsed.put(player.getUniqueId(), now);

        Stat stat = spell ? SPELL : ATTACK;

        int amount = RunicCore.getStatAPI().getStat(player.getUniqueId(), stat.getIdentifier());

        double constant = spell ? this.spellDamageConstant : this.attackDamageConstant;
        double multiplier = spell ? this.spellDamageMultiplier : this.attackDamageMultiplier;

        event.setAmount(event.getAmount() + (int) (amount * multiplier + constant));
        player.getWorld().spawnParticle(Particle.FLAME, event.getVictim().getLocation(), 30, 2 * Math.random(), 2 * Math.random(), 2 * Math.random());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.onDamage(event, event.getPlayer(), !event.isBasicAttack() && event.getSpell() != null);
    }

    @EventHandler(ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.onDamage(event, event.getPlayer(), event.getSpell() != null);
    }
}
