package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player is damaged by a magic source.
 * This gets called in our DamageUtil.
 */
public class SpellCastEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player caster;
    private final Entity[] recipients;
    private Spell spellCasted;
    private boolean isCancelled;
    private boolean willExecute; // for tier sets

    public SpellCastEvent(Player caster, Spell spellCasted, Entity... recipients) {
        this.caster = caster;
        this.spellCasted = spellCasted;
        this.recipients = recipients;
        this.isCancelled = false;
        this.willExecute = true;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getCaster() {
        return this.caster;
    }

    public Spell getSpell() {
        return spellCasted;
    }

    @Nullable
    public Entity[] getRecipients() {
        return this.recipients;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public Spell getSpellCasted() {
        return spellCasted;
    }

    public void setSpellCasted(Spell spellCasted) {
        this.spellCasted = spellCasted;
    }

    public boolean willExecute() {
        return willExecute;
    }

    public void setWillExecute(boolean willExecute) {
        this.willExecute = willExecute;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
