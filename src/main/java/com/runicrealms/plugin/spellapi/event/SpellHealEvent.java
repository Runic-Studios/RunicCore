package com.runicrealms.plugin.spellapi.event;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player is healed by a spell.
 * This gets called in our HealUtil.
 */
public class SpellHealEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity entity;
    private final Spell spell;
    private int amount;
    private boolean isCritical;
    private boolean isCancelled;

    public SpellHealEvent(int amount, Entity recipient, Player caster, @Nullable Spell spell) {
        this.amount = amount;
        this.entity = recipient;
        this.player = caster;
        this.spell = spell;
        this.isCritical = false;
        this.isCancelled = false;
    }

    public SpellHealEvent(int amount, Entity recipient, Player caster) {
        this(amount, recipient, caster, null);
    }

    public static HandlerList getHandlerList() {
        return handlers;
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

    @Nullable
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

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
