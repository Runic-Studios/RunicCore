package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.RunicItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This class checks when players damage a boss tracked by boss timed loot manager in items and applies the damage.
 * This is necessary because the Physical and Magic damage events don't exist in items.
 */
public class BossTimedLootDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        RunicItems.getLootAPI().getBossTimedLootManager().trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs late
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        RunicItems.getLootAPI().getBossTimedLootManager().trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
    }

}
