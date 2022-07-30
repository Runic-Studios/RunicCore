package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.ItemType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Listener which prevents classes from wearing incorrect types of armor
 * (i.e., mages wearing plate, etc.)
 */
public class ArmorTypeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(ArmorEquipEvent e) {

        if (e.getType() == null) return;
        if (e.getNewArmorPiece() == null) return;
        if (e.getNewArmorPiece().getType().equals(Material.AIR)) return;

        ItemStack equippedItem = e.getNewArmorPiece();
        Player player = e.getPlayer();
        String className = RunicCoreAPI.getPlayerClass(player);

        ItemType armorType = ItemType.matchType(equippedItem);

        switch (armorType) {
            case PLATE:
                if (!className.equals("Warrior")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case GILDED:
                if (!className.equals("Cleric")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case MAIL:
                if (!className.equals("Archer")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case LEATHER:
                if (!className.equals("Rogue")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case CLOTH:
                if (!className.equals("Mage")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
        }
    }

    private String armorMessage(String className) {
        String s = "";
        switch (className) {
            case "Archer":
                s = (ChatColor.RED + "Archers can only equip mail armor.");
                break;
            case "Cleric":
                s = (ChatColor.RED + "Clerics can only equip gilded armor.");
                break;
            case "Mage":
                s = (ChatColor.RED + "Mages can only equip cloth armor.");
                break;
            case "Rogue":
                s = (ChatColor.RED + "Rogues can only equip leather armor.");
                break;
            case "Warrior":
                s = (ChatColor.RED + "Warriors can only equip plate armor.");
                break;
        }
        return s;
    }
}
