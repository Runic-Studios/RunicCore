package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when an entity receives damage from a mob. Currently only applies to monsters.
 * Called in DamageListener, rather than the util.
 */
public class MobDamageEvent extends RunicDamageEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity mob;
    private final boolean applyMechanics;

    /**
     * @param amount         the amount to be dealt to the player
     * @param mob            the mob who damaged the player
     * @param victim         the player who suffered damage
     * @param applyMechanics whether to apply knockback
     */
    public MobDamageEvent(int amount, @NotNull Entity mob, @NotNull LivingEntity victim, boolean applyMechanics) {
        super(victim, amount);
        this.mob = mob;
        this.applyMechanics = applyMechanics;
    }

    @NotNull
    public Entity getMob() {
        return this.mob;
    }

    public boolean shouldApplyMechanics() {
        return this.applyMechanics;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
