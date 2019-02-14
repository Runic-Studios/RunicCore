package us.fortherealm.plugin.item;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class HelmetListener implements Listener {

    @EventHandler
    public void onHelmetEquip(PlayerInteractEvent e) {

        Player pl = e.getPlayer();

        if (pl.getInventory().getItemInMainHand() == null) return;
        ItemStack helmet = pl.getInventory().getItemInMainHand();
        Material material = pl.getInventory().getItemInMainHand().getType();
        ItemMeta meta = pl.getInventory().getItemInMainHand().getItemMeta();
        int slot = pl.getInventory().getHeldItemSlot();
        if (material != Material.SHEARS) return;

        // helmet durabilities are: 5, 10, 15, 20
        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20) return;

        // sound effects
        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                break;
        }

        // prevention against the event twice-firing, make sure the helmet slot is empty
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (pl.getInventory().getHelmet() != null) return;

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
            pl.getInventory().setHelmet(helmet);
            pl.getInventory().setItem(slot, new ItemStack(Material.AIR));
            Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.HOTBAR, ArmorType.HELMET, null, helmet));
        }
    }

    @EventHandler
    public void onHelmetShiftEquip(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if (!e.getClick().isShiftClick()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        Player pl = (Player) e.getWhoClicked();

        ItemStack helmet = e.getCurrentItem();
        ItemMeta meta = e.getCurrentItem().getItemMeta();

        // helmet durabilities are: 5, 10, 15, 20
        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20) return;
        if (pl.getInventory().getHelmet() != null) return;

        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                break;
        }

        e.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, ArmorType.HELMET, null, helmet));
        pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
        e.setCurrentItem(new ItemStack(Material.AIR));
        pl.getInventory().setHelmet(helmet);
    }

    @EventHandler
    public void onHelmetDragEquip(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if (e.getClick().isShiftClick()) return;
        if (e.getCursor() == null) return;
        if (e.getCursor().getType() != Material.SHEARS) return;
        if (e.getSlot() != 39) return;

        Player pl = (Player) e.getWhoClicked();
        ItemStack helmet = e.getCursor();
        ItemMeta meta = helmet.getItemMeta();

        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20) return;

        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                break;
        }

        e.setCancelled(true);
        pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
        if (pl.getInventory().getHelmet() == null) {
            e.setCursor(new ItemStack(Material.AIR));
            pl.getInventory().setHelmet(helmet);
            Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, null, helmet));
        } else {
            ItemStack currentHelm = pl.getInventory().getHelmet();
            e.setCursor(currentHelm);
            pl.getInventory().setHelmet(helmet);
            Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, currentHelm, helmet));
        }
    }

    @EventHandler
    public void onHelmetUnequip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if (e.getCursor() != null && e.getCursor().getType() != Material.SHEARS && e.getCursor().getType() != Material.AIR) return;
        if (e.getSlot() != 39) return;
        Player pl = (Player) e.getWhoClicked();
        Bukkit.getPluginManager().callEvent(new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, e.getCurrentItem(), e.getCursor()));
    }
}
