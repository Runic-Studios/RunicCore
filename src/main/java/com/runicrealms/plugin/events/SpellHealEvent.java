package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player is healed by a spell.
 * This gets called in our HealUtil.
 */
public class SpellHealEvent extends Event implements Cancellable {

    private int amount;
    private final Player player;
    private final Entity entity;
    private final Spell spell;
    private boolean isCancelled;

    public SpellHealEvent(int amount, Entity recipient, Player caster, Spell... spell) {
        this.amount = amount;
        this.entity = recipient;
        this.player = caster;
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
