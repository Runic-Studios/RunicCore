package com.runicrealms.plugin.api;

import com.runicrealms.plugin.DungeonLocation;
import org.bukkit.Location;

import java.util.List;

public interface RegionAPI {

    /**
     * Checks whether the given location is within the given region
     *
     * @param location         to check
     * @param regionIdentifier the string identifier of region "azana"
     * @return true if the location is in the region
     */
    boolean containsRegion(Location location, String regionIdentifier);

    /**
     * Attempts to grab a dungeon location from the current location by checking the current region name
     * Returns null if no dungeon is found
     *
     * @param location of the player or entity
     * @return a dungeon location if found
     */
    DungeonLocation getDungeonFromLocation(Location location);

    /**
     * Returns a list of the names of all regions containing the given location
     *
     * @param location the location to query
     * @return a list of region names
     */
    List<String> getRegionIds(Location location);

    /**
     * Checks whether the given location is within a city
     *
     * @param location to check
     * @return true if it's within a city
     */
    boolean isSafezone(Location location);
}
