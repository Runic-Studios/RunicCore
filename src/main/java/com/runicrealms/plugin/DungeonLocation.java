package com.runicrealms.plugin;

import com.runicrealms.plugin.utilities.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public enum DungeonLocation {

    SEBATHS_CAVE
            ("sebathscave",
                    "Sebath's Cave",
                    new Location(Bukkit.getWorld("dungeons"), -1874.5, 177, -522.5, 90, 0)
            ),
    CRYSTAL_CAVERN
            (
                    "crystalcavern",
                    "Crystal Cavern",
                    new Location(Bukkit.getWorld("dungeons"), 1208.5, 74, -66.5, 180, 0)
            ),
    JORUNDRS_KEEP
            (
                    "jorundrskeep",
                    "Jorundr's Keep",
                    new Location(Bukkit.getWorld("dungeons"), -534.5, 120, -177.5, 180, 0)
            ),
    SUNKEN_LIBRARY
            (
                    "library",
                    "Sunken Library",
                    new Location(Bukkit.getWorld("dungeons"), -23.5, 31, 11.5, 270, 0)
            ),
    CRYPTS_OF_DERA
            (
                    "crypts",
                    "Crypts of Dera",
                    new Location(Bukkit.getWorld("dungeons"), 298.5, 87, 6.5, 0, 0)
            ),
    FROZEN_FORTRESS
            (
                    "fortress",
                    "Frozen Fortress",
                    new Location(Bukkit.getWorld("dungeons"), 32.5, 73, 87.5, 0, 0)
            );

    private final String identifier;
    private final String display;
    private final Location location;
    private final Location chestLocation; // used for boss drops
    private final BlockFace blockFace;

    DungeonLocation(String identifier, String display, Location location) {
        this.identifier = identifier;
        this.display = display;
        this.location = location;
        this.chestLocation = getDungeonChestLocation();
        this.blockFace = getDungeonChestBlockFace();
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

    public Location getChestLocation() {
        return chestLocation;
    }

    public BlockFace getChestBlockFace() {
        return blockFace;
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

    private Location getDungeonChestLocation() {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);
        try {
            String world = dungeonSection.getString("chest.world");
            double x = dungeonSection.getDouble("chest.x");
            double y = dungeonSection.getDouble("chest.y");
            double z = dungeonSection.getDouble("chest.z");
            return new Location(Bukkit.getWorld((world != null ? world : "dungeons")), x, y, z);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(ChatColor.DARK_RED + "Error loading dungeon yaml file!");
        }
        return null;
    }

    private BlockFace getDungeonChestBlockFace() {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);
        try {
            return BlockFace.valueOf(dungeonSection.getString("chest.blockFace").toUpperCase());
        } catch (NullPointerException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(ChatColor.DARK_RED + "Error loading dungeon yaml file!");
        }
        return null;
    }
}
