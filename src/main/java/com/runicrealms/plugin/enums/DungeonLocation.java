package com.runicrealms.plugin.enums;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum DungeonLocation {

    SEBATHS_CAVE("sebathscave", "Sebath's Cave",
            new Location(Bukkit.getWorld("Alterra"), -1874.5, 177, -522.5, 90, 0)),
    CRYSTAL_CAVERN("crystalcavern", "Crystal Cavern",
            new Location(Bukkit.getWorld("Alterra"), 1208.5, 74, -66.5, 180, 0)),
    JORUNDRS_KEEP("jorundrskeep", "Jorundr's Keep",
            new Location(Bukkit.getWorld("Alterra"), -534.5, 120, -177.5, 180, 0)),
    SUNKEN_LIBRARY("library", "Sunken Library",
            new Location(Bukkit.getWorld("Alterra"), -23.5, 31, 11.5, 270, 0)),
    CRYPTS_OF_DERA("crypts", "Crypts of Dera",
            new Location(Bukkit.getWorld("Alterra"), 298.5, 87, 6.5, 0, 0)),
    FROZEN_FORTRESS("fortress", "Frozen Fortress",
            new Location(Bukkit.getWorld("Alterra"), 32.5, 73, 87.5, 0, 0));

    private final String identifier;
    private final String display;
    private final Location location;

    DungeonLocation(String identifier, String display, Location location) {
        this.identifier = identifier;
        this.display = display;
        this.location = location;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplay() {
        return display;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Returns an enum based on a string identifier
     *
     * @param identifier the name of the dungeon
     * @return an enum
     */
    public static DungeonLocation getFromIdentifier(String identifier) {
        for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
            if (dungeonLocation.getIdentifier().equals(identifier))
                return dungeonLocation;
        }
        return null;
    }

    /**
     * Returns the location of dungeon based on its string identifier
     *
     * @param identifier the name of the dungeon
     * @return a Location object
     */
    public static Location getLocationFromIdentifier(String identifier) {
        for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
            if (dungeonLocation.identifier.equals(identifier))
                return dungeonLocation.getLocation();
        }
        return null;
    }
}
