package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when an Archer fires a custom arrow for their basic attack
 */
public class StaffAttackEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final RunicItemWeapon runicItemWeapon;
    private boolean isCancelled;

    /**
     * @param player          who attacked w/ the staff
     * @param runicItemWeapon involved in the event (the staff itself)
     */
    public StaffAttackEvent(Player player, RunicItemWeapon runicItemWeapon) {
        this.player = player;
        this.runicItemWeapon = runicItemWeapon;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public RunicItemWeapon getRunicItemWeapon() {
        return runicItemWeapon;
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
