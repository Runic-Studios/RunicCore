package com.runicrealms.plugin.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MinLevelListener implements Listener {

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {

        if (e.getType() == null) return;
        if (e.getNewArmorPiece() == null) return;
        if (e.getNewArmorPiece().getType().equals(Material.AIR)) return;

        Player pl = e.getPlayer();
        ItemStack equippedItem = e.getNewArmorPiece();
        int plLv = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel();
        int reqLv = (int) AttributeUtil.getCustomDouble(equippedItem, "required.level");

        if (plLv < reqLv) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            e.setCancelled(true);
            pl.sendMessage(ChatColor.RED + "You aren't a high enough level to equip this!");
        }
    }
}
