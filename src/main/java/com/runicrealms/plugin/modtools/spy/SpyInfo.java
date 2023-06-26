package com.runicrealms.plugin.modtools.spy;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A class that contains all information needed by the plugin for the spy
 *
 * @author BoBoBalloon
 * @since 6/24/23
 */
public class SpyInfo {
    private final UUID target;
    private final Location origin;
    private final BukkitTask task;
    private Location center;
    //cache of inventory here if player logs out


    public SpyInfo(@NotNull UUID target, @NotNull Location origin, @NotNull BukkitTask task, @NotNull Location center) {
        this.target = target;
        this.origin = origin;
        this.task = task;
        this.center = center;
    }

    /**
     * A method that returns the uuid of the user being spied on
     *
     * @return the uuid of the user being spied on
     */
    @NotNull
    public UUID getTarget() {
        return this.target;
    }

    /**
     * A method that returns the location of the spy before they were set into spy mode
     *
     * @return the location of the spy before they were set into spy mode
     */
    @NotNull
    public Location getOrigin() {
        return this.origin;
    }

    /**
     * Gets an instance of the repeating task that makes sure the spy is nearby the spied
     *
     * @return the repeating task that makes sure the spy is nearby the spied
     */
    @NotNull
    public BukkitTask getTask() {
        return this.task;
    }

    /**
     * A method that gets the current target location of the user being spied on
     *
     * @return the current target location of the user being spied on
     */
    @NotNull
    public Location getCenter() {
        return this.center;
    }

    /**
     * A method that sets the current target location of the user being spied on
     *
     * @param center the current target location of the user being spied on
     */
    public void setCenter(@NotNull Location center) {
        this.center = center;
    }
}
