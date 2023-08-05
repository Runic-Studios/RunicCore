package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called when a mage fires a particle bolt for their basic attack
 */
public class StaffAttackEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final RunicItemWeapon runicItemWeapon;
    private int range;
    private boolean isCancelled;

    /**
     * @param player          who attacked w/ the staff
     * @param runicItemWeapon involved in the event (the staff itself)
     */
    public StaffAttackEvent(@NotNull Player player, @NotNull RunicItemWeapon runicItemWeapon, int range) {
        this.player = player;
        this.runicItemWeapon = runicItemWeapon;
        this.range = range;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public RunicItemWeapon getRunicItemWeapon() {
        return runicItemWeapon;
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
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

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
