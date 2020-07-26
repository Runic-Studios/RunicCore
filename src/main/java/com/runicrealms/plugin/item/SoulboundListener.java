package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SoulboundListener implements Listener {

    /*
     * Prevents dropping of soulbound items (extra boolean for gold pouches since many items weren't soulbound
     */
    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();

        boolean isGoldPouch = false;
        if (e.getItemDrop().getItemStack().getItemMeta() != null) {
            ItemMeta meta = e.getItemDrop().getItemStack().getItemMeta();
            int durability = ((Damageable) meta).getDamage();
            if ((e.getItemDrop().getItemStack().getType() == Material.SHEARS && durability == 234)) {
                isGoldPouch = true;
            }
        }

        boolean isSoulbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound").equals("true");
        if ((isSoulbound || isGoldPouch) && pl.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
