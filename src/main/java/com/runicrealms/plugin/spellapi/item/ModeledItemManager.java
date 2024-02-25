package com.runicrealms.plugin.spellapi.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.event.ModeledItemCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Item;
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

public class ModeledItemManager implements Listener, ModeledItemAPI {
    private static final int DEGREE_THRESHOLD = 5;
    private static final double RAY_SIZE = 1.5D; // 1.0D with hitbox = 1
    private final Map<UUID, ModeledItem> activeModeledItems = new ConcurrentHashMap<>();

    public ModeledItemManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startCollisionCheckTask();
    }

    private void startCollisionCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeModeledItems.forEach((uuid, modeledItem) -> {
                    try {
                        // Remove item if duration has expired
                        if (System.currentTimeMillis() - modeledItem.getStartTime() > (modeledItem.getDuration() * 1000)) {
                            activeModeledItems.remove(modeledItem.getItem().getUniqueId());
                            modeledItem.destroy();
                            return;
                        }
                        // Check for collisions
                        Item item = modeledItem.getItem();

                        RayTraceResult rayTraceEntities = item.getWorld().rayTraceEntities(
                                item.getLocation(),
                                item.getVelocity(),
                                modeledItem.getHitboxScale(),
                                RAY_SIZE, // Increases collision detection radius by raySize unit(s) in all directions
                                modeledItem.getFilter()
                        );

                        RayTraceResult rayTraceBlocks = item.getWorld().rayTraceBlocks(
                                item.getLocation(),
                                item.getVelocity(),
                                modeledItem.getHitboxScale(),
                                FluidCollisionMode.NEVER,
                                true
                        );

                        if (rayTraceEntities != null && rayTraceEntities.getHitEntity() != null) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.ENTITY, (LivingEntity) rayTraceEntities.getHitEntity());
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (rayTraceBlocks != null && rayTraceBlocks.getHitBlock() != null) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.TERRAIN);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (item.isOnGround()) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.ON_GROUND);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (item.getVelocity().length() == 0) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.VELOCITY_ZERO);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (!item.isValid()) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.INVALID);
                            Bukkit.getPluginManager().callEvent(event);
                        } else if (hasVectorChangedDirection(modeledItem.getVector(), modeledItem.getItem().getVelocity())) {
                            ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.TERRAIN);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    } catch (IllegalArgumentException e) {
                        ModeledItemCollideEvent event = new ModeledItemCollideEvent(modeledItem, CollisionCause.VELOCITY_ZERO);
                        Bukkit.getPluginManager().callEvent(event);
                    }
                });
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 4L); // Check every 4 game ticks (5 times/sec)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModeledItemCollide(ModeledItemCollideEvent event) {
        if (event.getCollisionCause() != CollisionCause.ENTITY || !event.shouldPassThroughEnemies()) {
            activeModeledItems.remove(event.getModeledItem().getItem().getUniqueId());
            event.getModeledItem().destroy();
        }
    }

    @Override
    public void addModeledItemToManager(ModeledItem modeledItem) {
        activeModeledItems.put(modeledItem.getItem().getUniqueId(), modeledItem);
    }

    /**
     * Helper method to detect if the moving item bounces against terrain
     * Compares the original vector against some threshold
     *
     * @param initialVector the initial velocity of the item
     * @param currentVector the current velocity of the item
     * @return true if the item has changed direction
     */
    private boolean hasVectorChangedDirection(Vector initialVector, Vector currentVector) {
        double angle = initialVector.angle(currentVector);
        double thresholdRadians = Math.toRadians(DEGREE_THRESHOLD);
        return angle > thresholdRadians;
    }
}
