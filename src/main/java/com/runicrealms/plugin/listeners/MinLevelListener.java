package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.ItemType;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MinLevelListener implements Listener {

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        if (e.getType() == null)
            return;
        if (e.getNewArmorPiece() == null)
            return;
        if (e.getNewArmorPiece().getType().equals(Material.AIR))
            return;
        Player player = e.getPlayer();
        ItemStack equippedItem = e.getNewArmorPiece();
        if (ItemType.matchType(equippedItem) == ItemType.OFFHAND)
            return;
        int playerLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassLevel();
        int requiredLevel = ((RunicItemArmor) RunicItemsAPI.getRunicItemFromItemStack(equippedItem)).getLevel();
        if (playerLevel < requiredLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your level is too low to equip this!");
        }
    }

    @EventHandler
    public void onOffhandEquip(InventoryClickEvent e) {
        if (e.getSlot() != 40)
            return;
        if (e.getCursor() == null)
            return;
        if (ItemType.matchType(e.getCursor()) != ItemType.OFFHAND)
            return;
        Player player = (Player) e.getWhoClicked();
        int playerLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassLevel();
        int requiredLevel = ((RunicItemOffhand) RunicItemsAPI.getRunicItemFromItemStack(e.getCursor())).getLevel();
        if (playerLevel < requiredLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your level is too low to equip this!");
        }
    }
}
