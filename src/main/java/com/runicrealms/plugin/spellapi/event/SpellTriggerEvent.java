package com.runicrealms.plugin.spellapi.event;

import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.SpellSlot;
import com.runicrealms.plugin.spellapi.SpellTriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player triggers the spell cast UI
 */
public class SpellTriggerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final SpellTriggerType spellTriggerType;
    private final SpellSlot spellslot;
    private boolean isCancelled;
    private boolean willExecute; // for tier sets

    public SpellTriggerEvent(Player player, SpellSlot spellslot) {
        this.player = player;
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        boolean isArcher = className.equalsIgnoreCase("archer");
        this.spellTriggerType = isArcher ? SpellTriggerType.ARCHER : SpellTriggerType.DEFAULT;
        this.spellslot = spellslot;
        this.isCancelled = false;
        this.willExecute = true;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SpellTriggerType getSpellTriggerType() {
        return spellTriggerType;
    }

    public SpellSlot getSpellslot() {
        return spellslot;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
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
