package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class TeleportUtil {

    public static void teleportEntity(Entity en, Location location) {
        ((CraftEntity) en).getHandle().setPosition(location.getX(), location.getY(), location.getZ());
    }
}
