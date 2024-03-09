package com.runicrealms.plugin.spellapi.modeled;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ModeledStandAttached extends ModeledStand {

    /**
     * Creates a ModeledStand, which is used as a projectile library. Using custom resources and ModelEngine,
     * creates moving projectiles with custom hitboxes for use in spell design
     *
     * @param player          who fired/spawned the modeled stand
     * @param location        where the stand will spawn
     * @param vector          that the stand will follow (its velocity)
     * @param customModelData an integer which is used in the '/models' folder's .json files to specify texture
     * @param duration        the maximum length before the stand will be destroyed
     * @param hitboxScale     a modifier to scale the custom hitbox up or down
     * @param standSlot       whether the item will spawn ARM or HEAD
     * @param validTargets    a predicate filter to specify valid targets (allies or enemies)
     */
    public ModeledStandAttached(Player player, Location location, Vector vector, int customModelData, double duration, double hitboxScale, StandSlot standSlot, Predicate<Entity> validTargets) {
        super(player, location, vector, customModelData, duration, hitboxScale, standSlot, validTargets);
    }

    @Override
    protected ArmorStand createArmorStand() {
        ArmorStand armorStand = super.createArmorStand();
        this.player.addPassenger(armorStand); // Mount the attached stand to the player
        /*
        Since we don't have to .teleport() or modify the stand's velocity, we can set it as a marker.
        This gives it a very small collision box
         */
        armorStand.setMarker(true);
        return armorStand;
    }

    @Override
    public void destroy() {
        this.player.removePassenger(this.armorStand);
        this.modeledEntity.destroy();
        this.armorStand.remove();
    }
}
