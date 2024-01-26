package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a custom SpellEffect is applied (Atone, Bleed, etc.)
 */
public class SpellEffectEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final SpellEffect spellEffect;
    private boolean isCancelled;

    public SpellEffectEvent(SpellEffect spellEffect) {
        this.spellEffect = spellEffect;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SpellEffect getSpellEffect() {
        return spellEffect;
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
