package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.common.event.ModelInteractEvent;
import com.runicrealms.plugin.spellapi.SpellSlot;
import com.runicrealms.plugin.spellapi.event.SpellTriggerEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Used to trigger spells when interacting with models
 */
public class ModelInteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onModelInteract(ModelInteractEvent event) {
        Player player = event.getWhoClicked();
        WeaponType heldItemType = WeaponType.matchType(player.getInventory().getItemInMainHand());
        if (heldItemType == WeaponType.NONE) return;
        if (heldItemType == WeaponType.GATHERING_TOOL) return;
        if (!DamageListener.matchClass(player, false)) return;

        // Determine spell slot from event
        SpellSlot spellSlot = event.getInteractionType() == ModelInteractEvent.InteractType.LEFT_CLICK
                ? SpellSlot.LEFT_CLICK
                : SpellSlot.RIGHT_CLICK;
        SpellTriggerEvent spellTriggerEvent = new SpellTriggerEvent(
                event.getWhoClicked(),
                spellSlot
        );
        // Call event sync
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(spellTriggerEvent));
    }
}
