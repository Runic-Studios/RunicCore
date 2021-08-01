package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player is damaged by a magic source.
 * This gets called in our DamageUtil.
 */
public class SpellDamageEvent extends RunicDamageEvent implements Cancellable {

    private final Player player;
    private final Spell spell;
    private boolean isCancelled;

    /**
     * Constructor for any event which causes spell damage to a player or mob
     *
     * @param amount  of damage to inflict
     * @param victim  who is receiving damage
     * @param damager player who is causing damage
     * @param spell   optional parameter to specify a spell source (for damage scaling)
     */
    public SpellDamageEvent(int amount, LivingEntity victim, Player damager, Spell... spell) {
        super(victim, amount);
        this.player = damager;
        this.spell = spell.length > 0 ? spell[0] : null;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Spell getSpell() {
        return this.spell;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    private static final HandlerList handlers = new HandlerList();

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
