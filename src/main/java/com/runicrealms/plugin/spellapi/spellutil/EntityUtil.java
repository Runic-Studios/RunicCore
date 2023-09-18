package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A utility class for getting entities around an origin
 *
 * @author BoBoBalloon
 */
public final class EntityUtil {
    private EntityUtil() {

    }

    /**
     * A method used to get the entities in a cone in front of the player (does not include player)
     * Credit to ChatGPT for the vector math and shit
     *
     * @param player    the player
     * @param radius    the distance/area in front of the player (radius of the semi-circle)
     * @param degrees   the degrees this cone should rotate around the player
     * @param predicate conditions entities must meet to be included in the final list
     * @return the entities in a cone in front of the player
     */
    @NotNull
    public static List<Entity> getEnemiesInCone(@NotNull Player player, float radius, double degrees, @Nullable Predicate<Entity> predicate) {
        List<Entity> entitiesInCone = new ArrayList<>();
        Vector direction = player.getLocation().getDirection();

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity.equals(player) || (predicate != null && !predicate.test(entity))) {
                continue;
            }

            Vector relative = entity.getLocation().subtract(player.getLocation()).toVector();

            // Checking line of sight
            boolean hasLineOfSight = true;
            for (Block block : player.getLineOfSight(null, Math.round(radius))) {
                if (block.getLocation().distance(entity.getLocation()) < 1.0) {
                    hasLineOfSight = false;
                    break;
                }
            }

            if (Math.acos(direction.dot(relative) / (direction.length() * relative.length())) <= degrees && hasLineOfSight) {
                entitiesInCone.add(entity);
            }
        }

        return entitiesInCone;
    }

    /**
     * A method used to get the entities in a cone in front of the player (does not include player)
     * Credit to ChatGPT for the vector math and shit
     *
     * @param player  the player
     * @param radius  the distance/area in front of the player (radius of the semi-circle)
     * @param degrees the degrees this cone should rotate around the player
     * @return the entities in a cone in front of the player
     */
    @NotNull
    public static List<Entity> getEnemiesInCone(@NotNull Player player, float radius, double degrees) {
        return EntityUtil.getEnemiesInCone(player, radius, degrees, null);
    }
}
