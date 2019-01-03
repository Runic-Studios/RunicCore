package us.fortherealm.plugin.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.classes.ArmorTypeEnum;

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
        String className = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");

        ArmorTypeEnum armorType = ArmorTypeEnum.matchType(equippedItem);

        switch (armorType) {
            case CLOTH:
                break;
            case LEATHER:
                if (className.equals("Mage")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip leather!");
                }
                break;
            case MAIL:
                if (className.equals("Archer") || className.equals("Mage") || className.equals("Rogue")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip mail!");
                }
                break;
            case PLATE:
                if (!className.equals("Warrior")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip plate!");
                }
                break;
            case CRYSTAL:
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                e.setCancelled(true);
                break;
        }
    }
}
