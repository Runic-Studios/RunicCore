package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.event.ModeledSpellCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
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

public class ModeledSpellManager implements Listener, ModeledSpellAPI {
    private static final double RAY_SIZE = 1.0D;
    private final Map<UUID, ModeledSpell> activeModeledSpells = new ConcurrentHashMap<>();

    public ModeledSpellManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startCollisionCheckTask();
    }

    private void startCollisionCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeModeledSpells.forEach((uuid, modeledSpell) -> {
                    // Remove modeled spell if duration has expired
                    if (System.currentTimeMillis() - modeledSpell.getStartTime() > (modeledSpell.getDuration() * 1000)) {
                        activeModeledSpells.remove(modeledSpell.getModeledEntity().getBase().getUUID());
                        modeledSpell.destroy();
                        return;
                    }

                    // Skip models with no vector or not a projectile
                    if (!(modeledSpell instanceof ModeledSpellProjectile projectile)) return;
                    if (projectile.getVector().length() == 0) return;

                    // If projectile has a maxDistance, calculate distance squared from starting point
                    if (projectile.getMaxDistance() != -1) {
                        double distanceSquared = projectile.getSpawnLocation().distanceSquared(projectile.getEntity().getLocation());
                        if (distanceSquared > Math.pow(projectile.getMaxDistance(), 2)) {
                            // If max distance exceeded, set velocity to zero and skip further processing
                            projectile.getEntity().setVelocity(new Vector(0, 0, 0));
                            ModeledSpellCollideEvent event = new ModeledSpellCollideEvent(modeledSpell, CollisionCause.MAX_DISTANCE);
                            Bukkit.getPluginManager().callEvent(event);
                            return;
                        }
                    }

                    Entity entity = modeledSpell.getEntity();
                    // Continuously set the base entity's velocity if type is LINEAR
                    if (projectile.getProjectileType() == ProjectileType.LINEAR) {
                        entity.setVelocity(projectile.getVector());
                    }

                    RayTraceResult rayTraceEntities = entity.getWorld().rayTraceEntities(
                            entity.getLocation(),
                            projectile.getVector(),
                            modeledSpell.getHitboxScale() + 0.5,
                            RAY_SIZE, // Increases collision detection radius by raySize unit(s) in all directions
                            modeledSpell.getFilter()
                    );

                    RayTraceResult rayTraceBlocks = entity.getWorld().rayTraceBlocks(
                            entity.getLocation(),
                            projectile.getVector(),
                            modeledSpell.getHitboxScale(),
                            FluidCollisionMode.NEVER,
                            true
                    );

                    handleCollision(modeledSpell, entity, rayTraceEntities, rayTraceBlocks);
                });
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 3L);
    }

    /**
     * Handles each case which will result in a ModeledStand collision, such as hitting entities or terrain
     */
    private void handleCollision(ModeledSpell modeledSpell, Entity entity, RayTraceResult rayTraceEntities, RayTraceResult rayTraceBlocks) {
        if (rayTraceEntities != null && rayTraceEntities.getHitEntity() != null) {
            ModeledSpellCollideEvent event = new ModeledSpellCollideEvent(modeledSpell, CollisionCause.ENTITY, (LivingEntity) rayTraceEntities.getHitEntity());
            Bukkit.getPluginManager().callEvent(event);
        } else if (rayTraceBlocks != null && rayTraceBlocks.getHitBlock() != null) {
            ModeledSpellCollideEvent event = new ModeledSpellCollideEvent(modeledSpell, CollisionCause.TERRAIN);
            Bukkit.getPluginManager().callEvent(event);
        } else if (entity.isOnGround()) {
            ModeledSpellCollideEvent event = new ModeledSpellCollideEvent(modeledSpell, CollisionCause.ON_GROUND);
            Bukkit.getPluginManager().callEvent(event);
        } else if (!entity.isValid()) {
            ModeledSpellCollideEvent event = new ModeledSpellCollideEvent(modeledSpell, CollisionCause.INVALID);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    /**
     * Remove the modeled entity and base on a collision
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModeledSpellCollide(ModeledSpellCollideEvent event) {
        event.getModeledSpell().cancel();
    }

    @Override
    public void addModeledSpellToManager(ModeledSpell modeledSpell) {
        this.activeModeledSpells.put(modeledSpell.getModeledEntity().getBase().getUUID(), modeledSpell);
    }
}
