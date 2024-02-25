package com.runicrealms.plugin.spellapi.armorstand;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.event.ModeledStandCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModeledStandManager implements Listener, ModeledStandAPI {
    private static final int DEGREE_THRESHOLD = 5;
    private static final double RAY_SIZE = 1.0D; // 1.0D with hitbox = 1
    private final Map<UUID, ModeledStand> activeModeledStands = new ConcurrentHashMap<>();

    public ModeledStandManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startCollisionCheckTask();
    }

    private void startCollisionCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeModeledStands.forEach((uuid, modeledStand) -> {
                    try {
                        // Remove stand if duration has expired
                        if (System.currentTimeMillis() - modeledStand.getStartTime() > (modeledStand.getDuration() * 1000)) {
                            activeModeledStands.remove(modeledStand.getArmorStand().getUniqueId());
                            modeledStand.destroy();
                            return;
                        }
                        // Check for collisions
                        ArmorStand armorStand = modeledStand.getArmorStand();
                        // Continuously set velocity
                        armorStand.setVelocity(modeledStand.getVector());

                        RayTraceResult rayTraceEntities = armorStand.getWorld().rayTraceEntities(
                                armorStand.getLocation(),
                                armorStand.getVelocity(),
                                modeledStand.getHitboxScale() + 0.5,
                                RAY_SIZE, // Increases collision detection radius by raySize unit(s) in all directions
                                modeledStand.getFilter()
                        );

                        RayTraceResult rayTraceBlocks = armorStand.getWorld().rayTraceBlocks(
                                armorStand.getLocation(),
                                armorStand.getVelocity(),
                                modeledStand.getHitboxScale(),
                                FluidCollisionMode.NEVER,
                                true
                        );

                        if (rayTraceEntities != null && rayTraceEntities.getHitEntity() != null) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.ENTITY, (LivingEntity) rayTraceEntities.getHitEntity());
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (rayTraceBlocks != null && rayTraceBlocks.getHitBlock() != null) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.TERRAIN);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (armorStand.isOnGround()) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.ON_GROUND);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (armorStand.getVelocity().length() == 0) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.VELOCITY_ZERO);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (!armorStand.isValid()) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.INVALID);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (hasVectorChangedDirection(modeledStand.getVector(), modeledStand.getArmorStand().getVelocity())) {
                            ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.TERRAIN);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    } catch (IllegalArgumentException e) {
                        ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.VELOCITY_ZERO);
                        Bukkit.getPluginManager().callEvent(event);
                    }
                });
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 3L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModeledStandCollide(ModeledStandCollideEvent event) {
        if (event.getCollisionCause() != CollisionCause.ENTITY || !event.shouldPassThroughEnemies()) {
            activeModeledStands.remove(event.getModeledStand().getArmorStand().getUniqueId());
            event.getModeledStand().destroy();
        }
    }

    @Override
    public void addModeledStandToManager(ModeledStand modeledStand) {
        activeModeledStands.put(modeledStand.getArmorStand().getUniqueId(), modeledStand);
    }

    /**
     * Helper method to detect if the moving stand bounces against terrain
     * Compares the original vector against some threshold
     *
     * @param initialVector the initial velocity of the stand
     * @param currentVector the current velocity of the stand
     * @return true if the stand has changed direction
     */
    private boolean hasVectorChangedDirection(Vector initialVector, Vector currentVector) {
        double angle = initialVector.angle(currentVector);
        double thresholdRadians = Math.toRadians(DEGREE_THRESHOLD);
        return angle > thresholdRadians;
    }
}
