package com.runicrealms.plugin.spellapi.modeled;

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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModeledStandManager implements Listener, ModeledStandAPI {
    private static final double RAY_SIZE = 1.0D;
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
                    // Remove stand if duration has expired
                    if (System.currentTimeMillis() - modeledStand.getStartTime() > (modeledStand.getDuration() * 1000)) {
                        activeModeledStands.remove(modeledStand.getArmorStand().getUniqueId());
                        modeledStand.destroy();
                        return;
                    }

                    // Ignore zero-vector collisions
                    if (modeledStand.getVector().length() == 0) return;

                    // Check for collisions
                    ArmorStand armorStand = modeledStand.getArmorStand();

                    // Continuously teleport stand to achieve movement
                    armorStand.teleport(armorStand.getLocation().add(modeledStand.getVector()));

                    // TODO: extract to method
                    RayTraceResult rayTraceEntities = armorStand.getWorld().rayTraceEntities(
                            armorStand.getLocation(),
                            modeledStand.getVector(),
                            modeledStand.getHitboxScale() + 0.5,
                            RAY_SIZE, // Increases collision detection radius by raySize unit(s) in all directions
                            modeledStand.getFilter()
                    );

                    RayTraceResult rayTraceBlocks = armorStand.getWorld().rayTraceBlocks(
                            armorStand.getLocation(),
                            modeledStand.getVector(),
                            modeledStand.getHitboxScale(),
                            FluidCollisionMode.NEVER,
                            true
                    );

                    // TODO: extract to method
                    if (rayTraceEntities != null && rayTraceEntities.getHitEntity() != null) {
                        ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.ENTITY, (LivingEntity) rayTraceEntities.getHitEntity());
                        Bukkit.getPluginManager().callEvent(event);
                    } else if (rayTraceBlocks != null && rayTraceBlocks.getHitBlock() != null) {
                        ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.TERRAIN);
                        Bukkit.getPluginManager().callEvent(event);
                    } else if (armorStand.isOnGround()) {
                        ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.ON_GROUND);
                        Bukkit.getPluginManager().callEvent(event);
                    } else if (!armorStand.isValid()) {
                        ModeledStandCollideEvent event = new ModeledStandCollideEvent(modeledStand, CollisionCause.INVALID);
                        Bukkit.getPluginManager().callEvent(event);
                    }

                });
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 3L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModeledStandCollide(ModeledStandCollideEvent event) {
        activeModeledStands.remove(event.getModeledStand().getArmorStand().getUniqueId());
        event.getModeledStand().destroy();
    }

    @Override
    public void addModeledStandToManager(ModeledStand modeledStand) {
        activeModeledStands.put(modeledStand.getArmorStand().getUniqueId(), modeledStand);
    }
}
