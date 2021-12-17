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
                    "HeadOfSebath"
            ),
    CRYSTAL_CAVERN
            (
                    "crystalcavern",
                    "Crystal Cavern",
                    "HeadOfHexagonis"
            ),
    JORUNDRS_KEEP
            (
                    "jorundrskeep",
                    "Jorundr's Keep",
                    "HeadOfJorundr"
            ),
    SUNKEN_LIBRARY
            (
                    "library",
                    "Sunken Library",
                    "HeadOfTheLibrarian"
            ),
    CRYPTS_OF_DERA
            (
                    "crypts",
                    "Crypts of Dera",
                    "HeadOfThePharaoh"
            ),
    FROZEN_FORTRESS
            (
                    "fortress",
                    "Frozen Fortress",
                    "HeadOfEldrid"
            );

    private final String identifier;
    private final String display;
    private final String currencyTemplateId;
    private final Location location;
    private final Location chestLocation; // used for boss drops
    private final BlockFace blockFace;

    DungeonLocation(String identifier, String display, String currencyTemplateId) {
        this.identifier = identifier;
        this.display = display;
        this.currencyTemplateId = currencyTemplateId;
        this.location = loadLocationFromFile("");
        this.chestLocation = loadLocationFromFile("chest.");
        this.blockFace = loadChestBlockFaceFromFile();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplay() {
        return display;
    }

    public String getCurrencyTemplateId() {
        return currencyTemplateId;
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

    private Location loadLocationFromFile(String prefix) {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);
        try {
            String world = dungeonSection.getString(prefix + "world");
            double x = dungeonSection.getDouble(prefix + "x");
            double y = dungeonSection.getDouble(prefix + "y");
            double z = dungeonSection.getDouble(prefix + "z");
            return new Location(Bukkit.getWorld((world != null ? world : "dungeons")),
                    x,
                    y,
                    z,
                    (float) dungeonSection.getDouble("yaw"),
                    (float) dungeonSection.getDouble("pitch")
            );
        } catch (NullPointerException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(ChatColor.DARK_RED + "Error loading dungeon yaml file!");
        }
        return null;
    }

    private BlockFace loadChestBlockFaceFromFile() {
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
