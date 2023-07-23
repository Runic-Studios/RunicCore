package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player is shielded by a spell.
 */
public class SpellShieldEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Player recipient;
    private final Spell spell;
    private int amount;
    private boolean isCancelled;

    /**
     * @param amount    of the shield
     * @param recipient of the spell
     * @param caster    who cast the spell
     * @param spell     optional parameter to specify which spell caused the shield. if specified, activates auto-scaling based on shield-per-level
     */
    public SpellShieldEvent(int amount, @NotNull Player recipient, @NotNull Player caster, @Nullable Spell spell) {
        this.amount = amount;
        this.recipient = recipient;
        this.player = caster;
        this.spell = spell;
        this.isCancelled = false;
    }

    public SpellShieldEvent(int amount, @NotNull Player recipient, @NotNull Player caster) {
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

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public Player getRecipient() {
        return this.recipient;
    }

    @Nullable
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
}
