package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.event.ModeledSpellCollideEvent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
                        modeledSpell.getModeledEntity().destroy();
                        return;
                    }

                    // Skip models with no vector
                    if (!(modeledSpell instanceof ModeledSpellProjectile projectile)) return;
                    if (projectile.getVector().length() == 0)
                        return;

                    // Continuously set the base entity's velocity
                    Entity entity = modeledSpell.getEntity();
                    entity.setVelocity(projectile.getVector());

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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModeledSpellCollide(ModeledSpellCollideEvent event) {
        /*
        Can't add the model's entity to the filter predicate before it is spawned into the world
        This prevents the entity from counting its own base in the raytrace
        */
        AtomicBoolean cancelEvent = new AtomicBoolean(false);
        this.activeModeledSpells.forEach((uuid, modeledSpell) -> {
            if (event.getEntity().equals(modeledSpell.getEntity())) {
                event.setCancelled(true);
                cancelEvent.set(true);
            }
        });
        if (cancelEvent.get()) return;
        event.getModeledSpell().cancel();
    }

    // TODO; remove all active entities on shutdown

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        this.activeModeledSpells.forEach((uuid, modeledSpell) -> {
            if (event.getEntity().equals(modeledSpell.getEntity())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMagicDamage(MagicDamageEvent event) {
        this.activeModeledSpells.forEach((uuid, modeledSpell) -> {
            if (event.getVictim().equals(modeledSpell.getEntity())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        this.activeModeledSpells.forEach((uuid, modeledSpell) -> {
            if (event.getVictim().equals(modeledSpell.getEntity())) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public void addModeledSpellToManager(ModeledSpell modeledSpell) {
        this.activeModeledSpells.put(modeledSpell.getModeledEntity().getBase().getUUID(), modeledSpell);
    }
}
