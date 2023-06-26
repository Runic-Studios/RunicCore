package com.runicrealms.plugin.fieldboss;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.FieldBossAPI;
import com.runicrealms.plugin.common.RunicCommon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class FieldBossManager implements FieldBossAPI, Listener {

    private final Map<String, FieldBoss> bosses = new HashMap<>();

    public FieldBossManager() {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            File folder = RunicCommon.getConfigAPI().getSubFolder(RunicCore.getInstance().getDataFolder(), "field-bosses");
            for (File fieldBossFile : Objects.requireNonNull(folder.listFiles())) {
                if (!fieldBossFile.isDirectory() && (
                        fieldBossFile.getName().endsWith(".yml") ||
                                fieldBossFile.getName().endsWith(".yaml"))) {
                    FileConfiguration config = RunicCommon.getConfigAPI().getYamlConfigFromFile(fieldBossFile.getName(), folder);
                    try {
                        FieldBoss boss = loadFieldBossFromConfig(config);
                        bosses.put(boss.getIdentifier(), boss);
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR loading field boss " + fieldBossFile.getName() + ":");
                        exception.printStackTrace();
                    }
                }
            }
        });
    }

    private FieldBoss loadFieldBossFromConfig(FileConfiguration config) {
        String identifier = config.getString("identifier");
        if (identifier == null)
            throw new IllegalStateException("Field boss config does not contain key \"identifier\"!");
        String name = config.getString("name");
        if (name == null) throw new IllegalStateException("Field boss config does not contain key \"name\"!");
        String mmID = config.getString("mm-id");
        if (mmID == null) throw new IllegalStateException("Field boss config does not contain key \"mm-id\"!");
        Location domeCentre;
        double domeRadius;
        try {
            domeCentre = new Location(
                    Bukkit.getWorld(Objects.requireNonNull(config.getString("dome.center.world"))),
                    config.getDouble("dome.center.x"),
                    config.getDouble("dome.center.y"),
                    config.getDouble("dome.center.z")
            );
            domeRadius = config.getDouble("dome.radius");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Field boss config does not contain proper dome keys!");
        }
        Location tributeChest;
        try {
            tributeChest = new Location(
                    Bukkit.getWorld(Objects.requireNonNull(config.getString("tribute-chest.world"))),
                    config.getDouble("tribute-chest.x"),
                    config.getDouble("tribute-chest.y"),
                    config.getDouble("tribute-chest.z")
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Field boss config does not contain proper dome keys!");
        }
        Location circleCentre;
        double circleRadius;
        try {
            circleCentre = new Location(
                    Bukkit.getWorld(Objects.requireNonNull(config.getString("circle.center.world"))),
                    config.getDouble("circle.center.x"),
                    config.getDouble("circle.center.y"),
                    config.getDouble("circle.center.z")
            );
            circleRadius = config.getDouble("circle.radius");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Field boss config does not contain proper circle keys!");
        }
        return new FieldBoss(identifier,
                name,
                mmID,
                domeCentre,
                domeRadius,
                tributeChest,
                circleCentre,
                circleRadius);
    }

    @Override
    public Collection<FieldBoss> getFieldBosses() {
        return bosses.values();
    }

    @Override
    public FieldBoss getFieldBoss(String identifier) {
        if (!bosses.containsKey(identifier))
            throw new IllegalArgumentException("Cannot get field boss with identifier " + identifier + " because it doesn't exist!");
        return bosses.get(identifier);
    }
}
