package com.runicrealms.plugin;

import com.runicrealms.plugin.item.lootchests.BossChestTier;
import com.runicrealms.plugin.utilities.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public enum DungeonLocation {

    SEBATHS_CAVE
            ("sebathscave",
                    "Sebath's Cave",
                    "HeadOfSebath",
                    BossChestTier.SEBATHS_CAVE
            ),
    CRYSTAL_CAVERN
            (
                    "crystalcavern",
                    "Crystal Cavern",
                    "HeadOfHexagonis",
                    BossChestTier.CRYSTAL_CAVERN
            ),
    JORUNDRS_KEEP
            (
                    "jorundrskeep",
                    "Jorundr's Keep",
                    "HeadOfJorundr",
                    BossChestTier.JORUNDRS_KEEP
            ),
    SUNKEN_LIBRARY
            (
                    "library",
                    "Sunken Library",
                    "HeadOfTheLibrarian",
                    BossChestTier.SUNKEN_LIBRARY
            ),
    CRYPTS_OF_DERA
            (
                    "crypts",
                    "Crypts of Dera",
                    "HeadOfThePharaoh",
                    BossChestTier.CRYPTS_OF_DERA
            ),
    FROZEN_FORTRESS
            (
                    "fortress",
                    "Frozen Fortress",
                    "HeadOfEldrid",
                    BossChestTier.FROZEN_FORTRESS
            );

    private final String identifier;
    private final String display;
    private final String currencyTemplateId;
    private final BossChestTier bossChestTier;
    private final Location location;
    private final Location chestLocation; // used for boss drops
    private final Map<Integer, Location> checkpoints;
    private final BlockFace blockFace;

    DungeonLocation(String identifier, String display, String currencyTemplateId, BossChestTier bossChestTier) {
        this.identifier = identifier;
        this.display = display;
        this.currencyTemplateId = currencyTemplateId;
        this.bossChestTier = bossChestTier;
        this.location = loadLocationFromFile("");
        this.chestLocation = loadLocationFromFile("chest.");
        this.checkpoints = loadCheckpointsFromFile();
        this.blockFace = loadChestBlockFaceFromFile();
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

    public BossChestTier getBossChestTier() {
        return bossChestTier;
    }

    public Map<Integer, Location> getCheckpoints() {
        return checkpoints;
    }

    public BlockFace getChestBlockFace() {
        return blockFace;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public String getCurrencyTemplateId() {
        return currencyTemplateId;
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

    private Map<Integer, Location> loadCheckpointsFromFile() {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(this.identifier);
        ConfigurationSection checkpointsSection = dungeonSection.getConfigurationSection("checkpoints");
        Map<Integer, Location> checkpoints = new HashMap<>();
        if (checkpointsSection == null) {
            Bukkit.getLogger().info(ChatColor.RED + "Checkpoints for " + this.display + " failed to load.");
            return checkpoints;
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
        } catch (NullPointerException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(ChatColor.DARK_RED + "Error loading dungeon yaml file!");
        }
        return checkpoints;
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
}
