package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player successfully weapon attacks an enemy with their artifact,
 * i.e., it is not on cooldown. Called in the DamageUtil.
 * Can specify the cause of the event, ranged or melee, for use with on-hit runic spells,
 * or applying different knockback for ranged spells, etc.
 */
public class WeaponDamageEvent extends RunicDamageEvent implements Cancellable {

    private final Player player;
    private final boolean isBasicAttack;
    private final boolean isRanged;
    private final Spell spell;
    private boolean isCancelled;

    /**
     * Constructor of weapon damage event w/ all the info we need!
     *
     * @param amount        of damage to inflict
     * @param damager       player who is causing damage
     * @param victim        who is receiving damage
     * @param isBasicAttack whether the attack is a physical spell or a basic attack
     * @param isRanged      whether the attack is a ranged physical spell (archers)
     * @param spell         optional parameter to specify a spell source (for damage scaling)
     */
    public WeaponDamageEvent(int amount, Player damager, LivingEntity victim, boolean isBasicAttack, boolean isRanged, Spell... spell) {
        super(victim, amount);
        this.player = damager;
        this.isBasicAttack = isBasicAttack;
        this.isRanged = isRanged;
        this.spell = spell.length > 0 ? spell[0] : null;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Spell getSpell() {
        return this.spell;
    }

    public boolean isBasicAttack() {
        return this.isBasicAttack;
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
