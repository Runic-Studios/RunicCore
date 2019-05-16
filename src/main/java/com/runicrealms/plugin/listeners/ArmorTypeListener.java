package com.runicrealms.plugin.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.enums.ItemTypeEnum;

/**
 * Listener which prevents classes from wearing incorrect types of armor
 * (i.e., mages wearing plate, etc.)
 */
public class ArmorTypeListener implements Listener {

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {

        if (e.getType() == null) return;
        if (e.getNewArmorPiece() == null) return;
        if (e.getNewArmorPiece().getType().equals(Material.AIR)) return;

        ItemStack equippedItem = e.getNewArmorPiece();
        Player pl = e.getPlayer();
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");

        ItemTypeEnum armorType = ItemTypeEnum.matchType(equippedItem);

        switch (armorType) {
            case PLATE:
                if (!className.equals("Warrior")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip plate armor!");
                }
                break;
            case GUILDED:
                if (!className.equals("Cleric")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip guilded armor!");
                }
                break;
            case MAIL:
                if (!className.equals("Archer")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip mail armor!");
                }
                break;
            case LEATHER:
                if (!className.equals("Rogue")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip leather armor!");
                }
                break;
            case CLOTH:
                if (!className.equals("Mage")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip cloth armor!");
                }
                break;
        }
    }
}
