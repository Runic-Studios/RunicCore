package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This custom event is called when a player successfully weapon attacks an enemy with their artifact,
 * i.e., it is not on cooldown. Called in the DamageUtil.
 * Can specify the cause of the event, ranged or melee, for use with on-hit runic spells,
 * or applying different knockback for ranged spells, etc.
 */
public class WeaponDamageEvent extends Event implements Cancellable {

    private int amount;
    private final Player player;
    private final Entity entity;
    private final boolean isAutoAttack;
    private final boolean isRanged;
    private final Spell spell;
    private boolean isCancelled;

    /**
     * Create an event w/ all the info we need!
     * @param amount of damage to inflict
     * @param damager player who is causing damage
     * @param victim who is receiving damage
     * @param isAutoAttack whether the attack is a physical spell or a basic attack
     * @param isRanged whether the attack is a ranged physical spell (archers)
     */
    public WeaponDamageEvent(int amount, Player damager, Entity victim, boolean isAutoAttack, boolean isRanged, Spell... spell) {
        this.amount = amount;
        this.player = damager;
        this.entity = victim;
        this.isAutoAttack = isAutoAttack;
        this.isRanged = isRanged;
        this.spell = spell.length > 0 ? spell[0] : null;
        this.isCancelled = false;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Spell getSpell() {
        return this.spell;
    }

    public boolean isAutoAttack() {
        return this.isAutoAttack;
    }

    public boolean isRanged() {
        return this.isRanged;
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
