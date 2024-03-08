package com.runicrealms.plugin.spellapi.modeled;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * The type Modeled spell attached. Extension of stationary modeled spell which remains fixed as a passenger
 */
public class ModeledSpellAttached extends ModeledSpellStationary {

    /**
     * Instantiates a new Modeled spell stationary.
     *
     * @param player        the player
     * @param modelId       the model id
     * @param spawnLocation the spawn location
     * @param hitboxScale   the hitbox scale
     * @param duration      the duration
     * @param validTargets  the valid targets
     */
    public ModeledSpellAttached(Player player, String modelId, Location spawnLocation, double hitboxScale, double duration, Predicate<Entity> validTargets) {
        super(player, modelId, spawnLocation, hitboxScale, duration, validTargets);
    }

    @Override
    public Entity initializeBaseEntity(Location location) {
        ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        this.player.addPassenger(armorStand);
        return armorStand;
    }

    @Override
    public void cancel() {
        player.removePassenger(this.entity);
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }
}
