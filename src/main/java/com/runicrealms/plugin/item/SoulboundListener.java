package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SoulboundListener implements Listener {

    /*
     * Prevents dropping of soulbound items (extra boolean for gold pouches since many items weren't soulbound
     */
    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {
        boolean isSoulbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound").equals("true");
        if (isSoulbound && e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            e.getPlayer().sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
