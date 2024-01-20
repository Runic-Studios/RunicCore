package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that models the ravenous item perk
 *
 * @author BoBoBalloon
 */
public class TacticianPerk extends ItemPerkHandler {
    private final Map<UUID, AttributeModifier> buffed;
    private final double speedPercentIncrease;
    private final double damageTakenPercentIncrease;

    public TacticianPerk() {
        super("tactician");
        this.buffed = new HashMap<>();

        this.speedPercentIncrease = ((Number) this.config.get("speed-percent-increase")).doubleValue();
        this.damageTakenPercentIncrease = ((Number) this.config.get("damage-taken-percent-increase")).doubleValue();
    }

    @Override
    public void onChange(Player player, int stacks) {
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        if (instance == null) {
            return;
        }

        if (stacks > 0) {
            AttributeModifier modifier = new AttributeModifier("tactician", instance.getBaseValue() * this.speedPercentIncrease, AttributeModifier.Operation.ADD_NUMBER);
            this.buffed.put(player.getUniqueId(), modifier);
            instance.addModifier(modifier);
            return;
        }

        AttributeModifier modifier = this.buffed.remove(player.getUniqueId());

        if (modifier == null) {
            RunicCore.getInstance().getLogger().warning("There was no attribute modifer stored in memory so I guess " + player.getName() + " now has a perm speed buff...");
            return;
        }

        instance.removeModifier(modifier);
    }

    private void onDamage(@NotNull RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player) || player.getAbsorptionAmount() > 0 || !this.isActive(player)) {
            return;
        }

        double amount = event.getAmount() * (1 + this.damageTakenPercentIncrease);

        event.setAmount((int) amount);
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
