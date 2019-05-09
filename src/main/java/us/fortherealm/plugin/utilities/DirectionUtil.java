package us.fortherealm.plugin.utilities;

import org.bukkit.entity.Player;

public class DirectionUtil {

    public static String getDirection(Player pl) {
        double rotation = (pl.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "W"; // W
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NW"; // NW
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "N"; // N
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "NE"; // NE
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "E"; // E
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SE"; // SE
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "S"; // S
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "SW"; // SW
        } else if (337.5 <= rotation && rotation < 360.0) {
            return "W"; // W
        } else {
            return "Something went wrong";
        }
    }
}
