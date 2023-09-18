package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player successfully weapon attacks an enemy with their artifact,
 * i.e., it is not on cooldown. Called in the DamageUtil.
 * Can specify the cause of the event, ranged or melee, for use with on-hit runic spells,
 * or applying different knockback for ranged spells, etc.
 */
public class PhysicalDamageEvent extends RunicDamageEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final boolean isBasicAttack;
    private final boolean isRanged;
    private final Spell spell;
    private boolean isCritical;
    private boolean isCancelled;

    /**
     * Constructor of damage event w/ all the info we need!
     *
     * @param amount        of damage to inflict
     * @param damager       player who is causing damage
     * @param victim        who is receiving damage
     * @param isBasicAttack whether the attack is a physical spell or a basic attack
     * @param isRanged      whether the attack is a ranged physical spell (archers)
     * @param spell         optional parameter to specify a spell source (for damage scaling)
     */
    public PhysicalDamageEvent(int amount, @NotNull Player damager, @NotNull LivingEntity victim, boolean isBasicAttack, boolean isRanged, @Nullable Spell spell) {
        super(victim, amount);
        this.player = damager;
        this.isBasicAttack = isBasicAttack;
        this.isRanged = isRanged;
        this.spell = spell;
        this.isCritical = false;
        this.isCancelled = false;
    }

    public PhysicalDamageEvent(int amount, @NotNull Player damager, @NotNull LivingEntity victim, boolean isBasicAttack, boolean isRanged) {
        this(amount, damager, victim, isBasicAttack, isRanged, null);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @Nullable
    public Spell getSpell() {
        return this.spell;
    }

    public boolean isBasicAttack() {
        return this.isBasicAttack;
    }

    public boolean isRanged() {
        return this.isRanged;
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
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
