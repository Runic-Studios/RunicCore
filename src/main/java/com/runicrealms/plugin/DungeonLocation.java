package com.runicrealms.plugin;

import com.runicrealms.plugin.utilities.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;

public enum DungeonLocation {

    SEBATHS_CAVE
            ("sebathscave",
                    "Sebath's Cave"
            ),
    CRYSTAL_CAVERN
            (
                    "crystalcavern",
                    "Crystal Cavern"
            ),
    JORUNDRS_KEEP
            (
                    "jorundrskeep",
                    "Jorundr's Keep"
            ),
    SUNKEN_LIBRARY
            (
                    "library",
                    "Sunken Library"
            ),
    CRYPTS_OF_DERA
            (
                    "crypts",
                    "Crypts of Dera"
            ),
    IGNAROTHS_LAIR(
            "ignaroth",
            "Ignaroth's Lair"
    ),
    FROZEN_FORTRESS
            (
                    "fortress",
                    "Frozen Fortress"
            );

    private final String identifier;
    private final String display;
    private final Location location;
    private final Map<Integer, Location> checkpoints;

    DungeonLocation(String identifier, String display) {
        Location temp;
        this.identifier = identifier;
        this.display = display;
        temp = SafeZoneLocation.AZANA.getLocation(); // Default if something goes wrong
        this.checkpoints = new HashMap<>();
        try {
            temp = loadLocationFromFile();
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe("An error occurred during DungeonLocation location initialization!");
        }
        this.location = temp;
        try {
            this.checkpoints.putAll(loadCheckpointsFromFile());
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe("An error occurred during DungeonLocation checkpoint initialization!");
        }
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

    public Map<Integer, Location> getCheckpoints() {
        return checkpoints;
    }

    public String getDisplay() {
        return display;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Location getLocation() {
        return location;
    }

    private Map<Integer, Location> loadCheckpointsFromFile() throws InvalidConfigurationException {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);

        if (dungeonSection == null) {
            String errorMessage = "Dungeon section for '" + this.identifier + "' is missing in the configuration.";
            Bukkit.getLogger().severe(errorMessage);
            throw new InvalidConfigurationException(errorMessage);
        }

        ConfigurationSection checkpointsSection = dungeonSection.getConfigurationSection("checkpoints");
        Map<Integer, Location> checkpoints = new HashMap<>();

        if (checkpointsSection == null) {
            String errorMessage = "Checkpoints section for '" + this.display + "' is missing in the configuration.";
            Bukkit.getLogger().severe(errorMessage);
            throw new InvalidConfigurationException(errorMessage);
        }

        try {
            for (String entry : checkpointsSection.getKeys(false)) {
                String world = checkpointsSection.getString(entry + ".world");
                double x = checkpointsSection.getDouble(entry + ".x");
                double y = checkpointsSection.getDouble(entry + ".y");
                double z = checkpointsSection.getDouble(entry + ".z");
                Location location = new Location(Bukkit.getWorld((world != null ? world : "dungeons")),
                        x,
                        y,
                        z,
                        (float) checkpointsSection.getDouble(entry + ".yaw"),
                        (float) checkpointsSection.getDouble(entry + ".pitch")
                );
                checkpoints.put(Integer.valueOf(entry), location);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error loading checkpoints from dungeon yaml file: " + e.getMessage());
            throw e; // Re-throw the exception for higher-level handling if needed
        }
        return checkpoints;
    }

    private Location loadLocationFromFile() throws InvalidConfigurationException {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);

        if (dungeonSection == null) {
            String errorMessage = "Dungeon section for '" + this.identifier + "' is missing in the configuration.";
            Bukkit.getLogger().severe(errorMessage);
            throw new InvalidConfigurationException(errorMessage);
        }

        try {
            String world = dungeonSection.getString("world");
            double x = dungeonSection.getDouble("x");
            double y = dungeonSection.getDouble("y");
            double z = dungeonSection.getDouble("z");
            return new Location(Bukkit.getWorld((world != null ? world : "dungeons")),
                    x,
                    y,
                    z,
                    (float) dungeonSection.getDouble("yaw"),
                    (float) dungeonSection.getDouble("pitch")
            );
        } catch (NullPointerException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error loading dungeon yaml file!");
        }
        return null;
    }
}
