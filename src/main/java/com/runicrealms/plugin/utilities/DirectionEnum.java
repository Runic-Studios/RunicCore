package com.runicrealms.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public enum DirectionEnum {

    WEST,
    NORTHWEST,
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST;

    public static DirectionEnum getDirection(Player pl) {
        double rotation = (pl.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return DirectionEnum.WEST; // W
        } else if (22.5 <= rotation && rotation < 67.5) {
            return DirectionEnum.NORTHWEST; // NW
        } else if (67.5 <= rotation && rotation < 112.5) {
            return DirectionEnum.NORTH; // N
        } else if (112.5 <= rotation && rotation < 157.5) {
            return DirectionEnum.NORTHEAST; // NE
        } else if (157.5 <= rotation && rotation < 202.5) {
            return DirectionEnum.EAST; // E
        } else if (202.5 <= rotation && rotation < 247.5) {
            return DirectionEnum.SOUTHEAST; // SE
        } else if (247.5 <= rotation && rotation < 292.5) {
            return DirectionEnum.SOUTH; // S
        } else if (292.5 <= rotation && rotation < 337.5) {
            return DirectionEnum.SOUTHWEST; // SW
        } else if (337.5 <= rotation && rotation < 360.0) {
            return DirectionEnum.WEST; // W
        } else {
            Bukkit.getLogger().info(ChatColor.RED + "Something went wrong");
            return null;
        }
    }
}
