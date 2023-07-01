package com.runicrealms.plugin.loot;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.LootAPI;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

// today's sponsor is chat gpt!
public class LootManager implements LootAPI {
    /*
    TODO:
    - add particles
    - Add it to file pull
    - add loot quality
    - integrate model engine
     */

    private final Map<String, LootTable> lootTables = new HashMap<>();
    private final Map<String, LootChestTemplate> lootChestTemplates = new HashMap<>();
    private final Map<Location, RegenerativeLootChest> regenLootChests = new HashMap<>();
    private FileConfiguration regenLootChestsConfig;
    private File regenLootChestsFile;
    private int nextRegenLootChestID = 0;

    private ClientLootManager clientLootManager;

    public LootManager() {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            File lootFolder = new File(RunicCore.getInstance().getDataFolder(), "loot");

            // LOOT TABLES
            File lootTableFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "loot-tables");
            // Map of loot table identifier -> config
            Map<String, FileConfiguration> configs = new HashMap<>();
            // map of loot table identifier -> list of other loot table identifiers that are subtables of it
            Map<String, List<String>> configDependencies = new HashMap<>();

            // Get config files and their dependencies
            for (File lootTableFile : Objects.requireNonNull(lootTableFolder.listFiles())) {
                if (!lootTableFile.isDirectory() && (lootTableFile.getName().endsWith(".yml") || lootTableFile.getName().endsWith(".yaml"))) {
                    try {
                        FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(lootTableFile.getName(), lootTableFolder);
                        String identifier = Objects.requireNonNull(config.getString("identifier"));
                        List<String> deps = new ArrayList<>();
                        if (config.isList("subtables")) {
                            deps = config.getStringList("subtables");
                        }
                        configs.put(identifier, config);
                        configDependencies.put(identifier, deps);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot table " + lootTableFile.getName() + ":");
                        exception.printStackTrace();
                    }
                }
            }

            // Parse config files and dependencies in proper order
            try {
                List<String> sortedIdentifiers = topologicalSort(configDependencies);
                for (String identifier : sortedIdentifiers) {
                    FileConfiguration config = configs.get(identifier);
                    try {
                        LootTable lootTable = parseLootTable(config);
                        lootTables.put(identifier, lootTable);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot table " + identifier + ":");
                        exception.printStackTrace();
                    }
                }
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.SEVERE, "ERROR loading loot tables:");
                exception.printStackTrace();
            }


            // LOOT CHEST TEMPLATES
            File chestTypesFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "chest-types");

            for (File chestTypeFile : Objects.requireNonNull(chestTypesFolder.listFiles())) {
                if (!chestTypeFile.isDirectory() && (chestTypeFile.getName().endsWith(".yml") || chestTypeFile.getName().endsWith(".yaml"))) {
                    FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(chestTypeFile.getName(), chestTypesFolder);
                    LootChestTemplate lootChestTemplate = parseLootChestTemplate(config);
                    lootChestTemplates.put(lootChestTemplate.getIdentifier(), lootChestTemplate);
                }
            }

            // LOOT CHESTS.yml
            regenLootChestsFile = new File(lootFolder, "regenerative-chests.yml");
            if (!regenLootChestsFile.exists()) {
                try {
                    if (!regenLootChestsFile.createNewFile())
                        throw new IOException("Could not create regenerative-chests.yml file!");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
            try {
                regenLootChestsConfig = RunicCommon.getConfigAPI().getYamlConfigFromFile(regenLootChestsFile);
                nextRegenLootChestID = regenLootChestsConfig.getInt("next-id");
                if (regenLootChestsConfig.contains("chests") && regenLootChestsConfig.isConfigurationSection("chests")) {
                    for (String chestID : Objects.requireNonNull(regenLootChestsConfig.getConfigurationSection("chests")).getKeys(false)) {
                        try {
                            RegenerativeLootChest chest = parseRegenerativeLootChest(regenLootChestsConfig.getConfigurationSection("chests." + chestID), chestID);
                            regenLootChests.put(chest.getPosition().getLocation(), chest);
                        } catch (Exception exception) {
                            Bukkit.getLogger().log(Level.SEVERE, "ERROR loading regenerative-chests.yml chest ID " + chestID + ":");
                            exception.printStackTrace();
                        }
                    }
                }
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.SEVERE, "ERROR loading regenerative-chests.yml:");
                exception.printStackTrace();
            }

            // LOAD CLIENT CHESTS
            this.clientLootManager = new ClientLootManager(getRegenerativeLootChests());

            // LOAD TIMED LOOT
            Set<TimedLoot> timedLoot = new HashSet<>();
            File timedLootFolder = RunicCommon.getConfigAPI().getSubFolder(lootFolder, "timed-loot");

            for (File timedLootFile : Objects.requireNonNull(timedLootFolder.listFiles())) {
                if (!timedLootFile.isDirectory() && (timedLootFile.getName().endsWith(".yml") || timedLootFile.getName().endsWith(".yaml"))) {
                    try {
                        FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(timedLootFile.getName(), chestTypesFolder);
                        TimedLoot loot = parseTimedLoot(config);
                        timedLoot.add(loot);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR loading timed loot " + timedLootFile.getName() + ":");
                        exception.printStackTrace();
                    }
                }
            }

            new BossTimedLootManager(timedLoot.stream()
                    .filter(loot -> loot instanceof BossTimedLoot)
                    .map(loot -> (BossTimedLoot) loot)
                    .collect(Collectors.toSet()));

            new CustomTimedLootManager(timedLoot.stream()
                    .filter(loot -> loot instanceof CustomTimedLoot)
                    .map(loot -> (CustomTimedLoot) loot)
                    .collect(Collectors.toSet()));
        });
    }

    // The next two methods are responsible for dealing with the order in which we load loot tables
    // since now we have subtables as a feature
    private static void dfs(Map<String, List<String>> dependencies,
                            String identifier,
                            Map<String, Boolean> visited,
                            Map<String, Boolean> recursionStack,
                            List<String> result) {
        visited.put(identifier, true);
        recursionStack.put(identifier, true);
        for (String neighbor : dependencies.get(identifier)) {
            if (!visited.getOrDefault(neighbor, false)) {
                dfs(dependencies, neighbor, visited, recursionStack, result);
            } else if (recursionStack.get(neighbor)) {
                // A cycle is detected because the neighbor node is part of the recursion stack.
                throw new IllegalStateException("Circular dependency detected: " + identifier + " -> " + neighbor);
            }
        }
        recursionStack.put(identifier, false); // Remove the node from the recursion stack once it is finished.
        result.add(identifier);
    }

    private static List<String> topologicalSort(Map<String, List<String>> dependencies) {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> recursionStack = new HashMap<>();
        List<String> result = new ArrayList<>();
        for (String identifier : dependencies.keySet()) {
            if (!visited.getOrDefault(identifier, false)) {
                dfs(dependencies, identifier, visited, recursionStack, result);
            }
        }
        Collections.reverse(result); // reverse to get the correct order
        return result;
    }

    private LootTable parseLootTable(FileConfiguration config) {
        String identifier = Objects.requireNonNull(config.getString("identifier"));
        List<LootTable.LootItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        assert itemsSection != null;
        for (String templateID : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(templateID);
            assert section != null;
            int weight = section.getInt("weight");
            if (weight == 0) throw new IllegalArgumentException("Loot table has invalid weight for " + templateID);
            if (templateID.equalsIgnoreCase("script")) {
                items.add(new LootTable.LootScriptItem(weight));
            } else {
                if (!RunicItemsAPI.isTemplate(templateID))
                    throw new IllegalArgumentException("Loot table has invalid template ID " + templateID);
                int minStackSize = section.getInt("stack-size.min", 1);
                int maxStackSize = section.getInt("stack-size.max", 1);
                items.add(new LootTable.LootItem(templateID, weight, minStackSize, maxStackSize));
            }
        }

        if (config.isList("subtables")) {
            List<String> subtables = config.getStringList("subtables");
            for (String subtable : subtables) {
                LootTable sub = lootTables.get(subtable);
                Objects.requireNonNull(sub, "Subtable " + subtable + " doesn't exist");
                items.addAll(sub.getItems());
            }
        }

        return new LootTable(identifier, items);
    }

    private LootChestTemplate parseLootChestTemplate(FileConfiguration config) {
        String identifier = Objects.requireNonNull(config.getString("identifier"));
        String lootTable = Objects.requireNonNull(config.getString("loot-table"));
        if (!lootTables.containsKey(lootTable))
            throw new IllegalArgumentException("Loot chest template " + identifier + " has invalid loot table " + lootTable);
        int minCount = config.getInt("count.min");
        int maxCount = config.getInt("count.max");
        if (minCount == 0 || maxCount == 0)
            throw new IllegalArgumentException("Loot chest template " + identifier + " must have count.min and count.max!");
        return new LootChestTemplate(identifier, lootTables.get(lootTable), minCount, maxCount, 27);
    }

    private RegenerativeLootChest parseRegenerativeLootChest(ConfigurationSection section, String chestID) {
        if (section == null)
            throw new IllegalArgumentException("Chest " + chestID + " does not exist in configuration");
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        if (locationSection == null)
            throw new IllegalArgumentException("Location section missing for chest " + chestID);
        String world = locationSection.getString("world");
        int x = locationSection.getInt("x");
        int y = locationSection.getInt("y");
        int z = locationSection.getInt("z");
        BlockFace direction = BlockFace.valueOf(Objects.requireNonNull(locationSection.getString("direction")).toUpperCase());
        if (world == null || x == 0 || y == 0 || z == 0)
            throw new IllegalArgumentException("One or more location values missing for chest " + chestID);
        Location location = new Location(Objects.requireNonNull(Bukkit.getWorld(world), "World " + world + " does not exist for chest " + chestID), x, y, z);
        String chestTemplate = section.getString("template");
        int regenerationTime = section.getInt("regeneration-time");
        ConfigurationSection itemLevelSection = section.getConfigurationSection("item-level");
        if (itemLevelSection == null)
            throw new IllegalArgumentException("Item level section missing for chest " + chestID);
        int minLevel = section.getInt("min-level");
        int itemMinLevel = itemLevelSection.getInt("min");
        int itemMaxLevel = itemLevelSection.getInt("max");
        if (itemMinLevel == 0 || itemMaxLevel == 0)
            throw new IllegalArgumentException("One or more item level values missing for chest " + chestID);
        String title = section.getString("title");
        if (title == null)
            throw new IllegalArgumentException("Title missing for chest " + chestID);
        if (chestTemplate == null || regenerationTime == 0)
            throw new IllegalArgumentException("Chest template or regeneration time missing for chest " + chestID);
        LootChestConditions conditions;
        if (section.isConfigurationSection("conditions")) {
            conditions = LootChestConditions.loadFromConfig(Objects.requireNonNull(section.getConfigurationSection("conditions")));
        } else {
            conditions = new LootChestConditions();
        }
        return new RegenerativeLootChest(
                new LootChestPosition(location, direction),
                lootChestTemplates.get(chestTemplate),
                conditions,
                minLevel,
                itemMinLevel, itemMaxLevel,
                regenerationTime,
                title);
    }

    private TimedLoot parseTimedLoot(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("chest");
        if (section == null) throw new IllegalArgumentException("Timed loot needs to have chest key!");
        ConfigurationSection locationSection = section.getConfigurationSection("location");
        if (locationSection == null)
            throw new IllegalArgumentException("Location section missing for timed loot " + config.getName());
        String world = locationSection.getString("world");
        int x = locationSection.getInt("x");
        int y = locationSection.getInt("y");
        int z = locationSection.getInt("z");
        BlockFace direction = BlockFace.valueOf(Objects.requireNonNull(locationSection.getString("direction")).toUpperCase());
        if (world == null || x == 0 || y == 0 || z == 0)
            throw new IllegalArgumentException("One or more location values missing for timed loot " + config.getName());
        Location location = new Location(Objects.requireNonNull(Bukkit.getWorld(world), "Timed loot world " + world + " does not exist!"), x, y, z);
        String chestTemplate = section.getString("template");
        ConfigurationSection itemLevelSection = section.getConfigurationSection("item-level");
        if (itemLevelSection == null)
            throw new IllegalArgumentException("Item level section missing for timed loot " + config.getName());
        int minLevel = section.getInt("min-level");
        int itemMinLevel = itemLevelSection.getInt("min");
        int itemMaxLevel = itemLevelSection.getInt("max");
        if (itemMinLevel == 0 || itemMaxLevel == 0)
            throw new IllegalArgumentException("One or more item level values missing for timed loot " + config.getName());
        String title = section.getString("title");
        if (title == null)
            throw new IllegalArgumentException("Title missing for timed loot " + config.getName());
        if (chestTemplate == null)
            throw new IllegalArgumentException("Chest template or regeneration time missing for timed loot " + config.getName());
        LootChestConditions conditions;
        if (section.isConfigurationSection("conditions")) {
            conditions = LootChestConditions.loadFromConfig(Objects.requireNonNull(section.getConfigurationSection("conditions")));
        } else {
            conditions = new LootChestConditions();
        }
        int duration = section.getInt("duration");
        if (duration == 0)
            throw new IllegalArgumentException("Timed loot chest " + config.getName() + " missing duration!");
        ConfigurationSection hologramLocationSection = section.getConfigurationSection("hologram.location");
        if (hologramLocationSection == null)
            throw new IllegalArgumentException("Hologram location section missing for timed loot " + config.getName());
        String holoWorld = hologramLocationSection.getString("world");
        int holoX = hologramLocationSection.getInt("x");
        int holoY = hologramLocationSection.getInt("y");
        int holoZ = hologramLocationSection.getInt("z");
        if (holoWorld == null || holoX == 0 || holoY == 0 || holoZ == 0)
            throw new IllegalArgumentException("One or more hologram-location values missing for timed loot " + config.getName());
        Location hologramLocation = new Location(Objects.requireNonNull(Bukkit.getWorld(holoWorld), "Timed loot hologram location world " + holoWorld + " does not exist!"), holoX, holoY, holoZ);
        List<String> hologramLines = section.getStringList("hologram.lines");
        if (hologramLines.isEmpty())
            throw new IllegalArgumentException("Hologram.lines missing from timed loot " + config.getName());
        String type = config.getString("type");
        TimedLootChest chest = new TimedLootChest(
                new LootChestPosition(location, direction),
                lootChestTemplates.get(chestTemplate),
                conditions,
                minLevel,
                itemMinLevel, itemMaxLevel,
                title,
                duration,
                hologramLocation,
                (hologram, time) -> {
                    hologram.getLines().clear();
                    for (String line : hologramLines) {
                        hologram.getLines().appendText(ColorUtil.format(line.replaceAll("%text%", line)));
                    }
                });
        if ("boss".equalsIgnoreCase(type)) {
            String mmID = config.getString("boss.mm-id");
            double lootDamageThreshold = config.getDouble("boss.loot-damage-threshold");
            if (mmID == null)
                throw new IllegalArgumentException("Boss timed loot chest missing boss.mm-id");
            return new BossTimedLoot(chest, mmID, lootDamageThreshold);
        } else if ("custom".equalsIgnoreCase(type)) {
            String identifier = config.getString("custom.identifier");
            if (identifier == null)
                throw new IllegalArgumentException("Timed loot missing identifier " + config.getName());
            return new CustomTimedLoot(chest, identifier);
        } else throw new IllegalArgumentException("Bad type for timed loot chest: " + type);
    }

    @Override
    public void createRegenerativeLootChest(RegenerativeLootChest regenerativeLootChest) {
        int id = nextRegenLootChestID;
        regenLootChestsConfig.set("chests." + id + ".location.world", Objects.requireNonNull(regenerativeLootChest.getPosition().getLocation().getWorld()).getName());
        regenLootChestsConfig.set("chests." + id + ".location.x", regenerativeLootChest.getPosition().getLocation().getBlockX());
        regenLootChestsConfig.set("chests." + id + ".location.y", regenerativeLootChest.getPosition().getLocation().getBlockY());
        regenLootChestsConfig.set("chests." + id + ".location.z", regenerativeLootChest.getPosition().getLocation().getBlockZ());
        regenLootChestsConfig.set("chests." + id + ".location.direction", regenerativeLootChest.getPosition().getDirection().toString());
        regenLootChestsConfig.set("chests." + id + ".template", regenerativeLootChest.getLootChestTemplate().getIdentifier());
        regenLootChestsConfig.set("chests." + id + ".regeneration-time", regenerativeLootChest.getRegenerationTime());
        regenLootChestsConfig.set("chests." + id + ".min-level", regenerativeLootChest.getMinLevel());
        regenLootChestsConfig.set("chests." + id + ".item-level.min", regenerativeLootChest.getItemMinLevel());
        regenLootChestsConfig.set("chests." + id + ".item-level.max", regenerativeLootChest.getItemMaxLevel());
        regenLootChestsConfig.set("chests." + id + ".title", regenerativeLootChest.getInventoryTitle());
        if (regenerativeLootChest.getConditions().getConditionsList().size() > 0) {
            ConfigurationSection conditionsSection = regenLootChestsConfig.createSection("chests." + id + ".conditions");
            regenerativeLootChest.getConditions().addToConfig(conditionsSection);
        }
        nextRegenLootChestID++;
        regenLootChestsConfig.set("next-id", nextRegenLootChestID);
        saveRegenLootChestConfigAsync();
        regenLootChests.put(regenerativeLootChest.getPosition().getLocation(), regenerativeLootChest);
    }

    @Override
    public void deleteRegenerativeLootChest(RegenerativeLootChest regenerativeLootChest) {
        try {
            for (String key : Objects.requireNonNull(regenLootChestsConfig.getConfigurationSection("chests")).getKeys(false)) {
                if (Objects.requireNonNull(regenLootChestsConfig.getString("chests." + key + ".location.world"))
                        .equalsIgnoreCase(Objects.requireNonNull(regenerativeLootChest.getPosition().getLocation().getWorld()).getName())
                        && regenLootChestsConfig.getInt("chests." + key + ".location.x") == regenerativeLootChest.getPosition().getLocation().getBlockX()
                        && regenLootChestsConfig.getInt("chests." + key + ".location.y") == regenerativeLootChest.getPosition().getLocation().getBlockY()
                        && regenLootChestsConfig.getInt("chests." + key + ".location.z") == regenerativeLootChest.getPosition().getLocation().getBlockZ()) {
                    regenLootChestsConfig.set("chests." + key, null);
                    saveRegenLootChestConfigAsync();
                    return;
                }
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "ERROR removing regenerative loot chest from config:");
            throw exception;
        }
        throw new IllegalArgumentException("Cannot remove regenerative loot chest from config: none exists at given location!");
    }

    @Override
    public boolean isLootChestTemplate(String identifier) {
        return lootChestTemplates.containsKey(identifier);
    }

    @Override
    public Collection<LootChestTemplate> getChestTemplates() {
        return lootChestTemplates.values();
    }

    @Override
    public Collection<RegenerativeLootChest> getRegenerativeLootChests() {
        return regenLootChests.values();
    }

    @Override
    public void displayTimedLootChest(Player player, TimedLootChest chest) {
        clientLootManager.displayTimedLootChest(player, chest);
    }

    private void saveRegenLootChestConfigAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try {
                regenLootChestsConfig.save(regenLootChestsFile);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Override
    public LootTable getLootTable(String identifier) {
        if (!lootTables.containsKey(identifier))
            throw new IllegalArgumentException("Cannot get loot table " + identifier + " because it does not exist");
        return lootTables.get(identifier);
    }

    @Override
    public LootChestTemplate getLootChestTemplate(String identifier) {
        if (!lootChestTemplates.containsKey(identifier))
            throw new IllegalArgumentException("Cannot get loot chest template " + identifier + " because it does nto exist");
        return lootChestTemplates.get(identifier);
    }

    @Override
    public @Nullable RegenerativeLootChest getRegenerativeLootChest(Location location) {
        return regenLootChests.get(location);
    }

}