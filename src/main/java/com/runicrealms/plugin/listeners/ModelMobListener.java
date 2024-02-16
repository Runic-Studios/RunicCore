package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.event.ModelInteractEvent;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

/**
 * Used to create damage events for MythicMobs with ModelEngine models
 */
public class ModelMobListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onModelInteract(ModelInteractEvent event) {
        if (event.getInteractionType() == ModelInteractEvent.InteractType.RIGHT_CLICK) return;
        UUID entityId = event.getActiveModel().getModeledEntity().getBase().getUUID();
        if (MythicBukkit.inst().getMobManager().getActiveMob(entityId).isEmpty()) return;
        ActiveMob am = MythicBukkit.inst().getMobManager().getActiveMob(entityId).get();
        EntityDamageByEntityEvent damageByEntityEvent = new EntityDamageByEntityEvent(
                event.getWhoClicked(),
                am.getEntity().getBukkitEntity(),
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                1
        );
        // Call event sync
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(damageByEntityEvent));

    }
}
