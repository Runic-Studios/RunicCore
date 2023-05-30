package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a player is damaged by a magic source.
 * This gets called in our DamageUtil.
 */
public class MagicDamageEvent extends RunicDamageEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Spell spell;
    private boolean isCritical;
    private boolean isCancelled;

    /**
     * Constructor for any event which causes magic damage to a player or mob
     *
     * @param amount  of damage to inflict
     * @param victim  who is receiving damage
     * @param damager player who is causing damage
     * @param spell   optional parameter to specify a spell source (for damage scaling)
     */
    public MagicDamageEvent(int amount, LivingEntity victim, Player damager, Spell... spell) {
        super(victim, amount);
        this.player = damager;
        this.spell = spell.length > 0 ? spell[0] : null;
        this.isCritical = false;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Spell getSpell() {
        return this.spell;
    }

    public boolean isCritical() {
        return this.isCritical;
    }

    public void setCritical(boolean isCritical) {
        this.isCritical = isCritical;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    @NotNull
    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
