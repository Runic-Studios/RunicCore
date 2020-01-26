package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SoulboundListener implements Listener {

    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {

        Player pl = e.getPlayer();
        boolean isSoulbound = false;
        String soulbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound");
        if (soulbound.equals("true")) {
            isSoulbound = true;
        }

        if (isSoulbound && pl.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
