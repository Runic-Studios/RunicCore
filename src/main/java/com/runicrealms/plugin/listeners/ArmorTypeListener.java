package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.ItemType;
import com.runicrealms.plugin.common.event.ArmorEquipEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
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

    private String armorMessage(String className) {
        return switch (className) {
            case "Archer" -> (ChatColor.RED + "Archers can only equip mail armor.");
            case "Cleric" -> (ChatColor.RED + "Clerics can only equip gilded armor.");
            case "Mage" -> (ChatColor.RED + "Mages can only equip cloth armor.");
            case "Rogue" -> (ChatColor.RED + "Rogues can only equip leather armor.");
            case "Warrior" -> (ChatColor.RED + "Warriors can only equip plate armor.");
            default -> "";
        };
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(ArmorEquipEvent event) {
        if (event.isCancelled()) return;
        if (event.getType() == null) return;
        if (event.getNewArmorPiece() == null) return;
        if (event.getNewArmorPiece().getType().equals(Material.AIR)) return;

        ItemStack equippedItem = event.getNewArmorPiece();
        Player player = event.getPlayer();
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        ItemType itemType = ItemType.matchType(equippedItem);
        switch (itemType) {
            case PLATE:
                if (!className.equalsIgnoreCase("Warrior")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    event.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case GILDED:
                if (!className.equalsIgnoreCase("Cleric")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    event.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case MAIL:
                if (!className.equalsIgnoreCase("Archer")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    event.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case LEATHER:
                if (!className.equalsIgnoreCase("Rogue")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    event.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            case CLOTH:
                if (!className.equalsIgnoreCase("Mage")) {
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    event.setCancelled(true);
                    player.sendMessage(armorMessage(className));
                }
                break;
            default:
                break;
        }
    }
}
