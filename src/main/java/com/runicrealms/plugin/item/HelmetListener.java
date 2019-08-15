package com.runicrealms.plugin.item;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.runicrealms.plugin.events.MobDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.RunicCore;

public class HelmetListener implements Listener {

    @EventHandler
    public void onHelmetEquip(PlayerInteractEvent e) {

        Player pl = e.getPlayer();

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack helmet = pl.getInventory().getItemInMainHand();
        Material material = pl.getInventory().getItemInMainHand().getType();
        ItemMeta meta = pl.getInventory().getItemInMainHand().getItemMeta();
        int slot = pl.getInventory().getHeldItemSlot();
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (material != Material.SHEARS) return;

        // helmet durabilities are: 5, 10, 15, 20, 25
        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20 && durability != 25) return;

        // sound effects
        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Mage")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip cloth armor!");
                    return;
                }
                break;
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Rogue")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip leather armor!");
                    return;
                }
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                if (!className.equals("Archer")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip mail armor!");
                    return;
                }
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_GOLD;
                if (!className.equals("Cleric")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip gilded armor!");
                    return;
                }
                break;
            case 25:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                if (!className.equals("Warrior")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip plate armor!");
                    return;
                }
                break;
        }

        // prevention against the event twice-firing, make sure the helmet slot is empty
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (pl.getInventory().getHelmet() != null) return;

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ArmorEquipEvent newEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.HOTBAR, ArmorType.HELMET, null, helmet);
            Bukkit.getPluginManager().callEvent(newEvent);
            if (!newEvent.isCancelled()) {
                pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
                pl.getInventory().setHelmet(helmet);
                pl.getInventory().setItem(slot, new ItemStack(Material.AIR));
            }
        }
    }

    /**
     * Manages shift-clicking shears (helmets) in inventory.
     */
    @EventHandler
    public void onHelmetShiftEquip(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        if (!e.getClick().isShiftClick()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        InventoryView view = e.getWhoClicked().getOpenInventory();
        // prevents shift-clicking mechanics on item menus
        if (!view.getTopInventory().toString().contains("CraftInventoryCrafting")) return;

        Player pl = (Player) e.getWhoClicked();
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");

        ItemStack helmet = e.getCurrentItem();
        ItemMeta meta = e.getCurrentItem().getItemMeta();

        // helmet durabilities are: 5, 10, 15, 20
        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20 && durability != 25) return;
        if (pl.getInventory().getHelmet() != null) return;

        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Mage")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip cloth armor!");
                    return;
                }
                break;
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Rogue")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip leather armor!");
                    return;
                }
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                if (!className.equals("Archer")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip mail armor!");
                    return;
                }
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_GOLD;
                if (!className.equals("Cleric")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip gilded armor!");
                    return;
                }
                break;
            case 25:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                if (!className.equals("Warrior")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    e.setCancelled(true);
                    pl.sendMessage(ChatColor.RED + className + "s aren't trained to equip plate armor!");
                    return;
                }
                break;
        }

        e.setCancelled(true);
        ArmorEquipEvent newEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.SHIFT_CLICK, ArmorType.HELMET, null, helmet);
        Bukkit.getPluginManager().callEvent(newEvent);
        if (!newEvent.isCancelled()) {
            pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
            e.setCurrentItem(new ItemStack(Material.AIR));
            pl.getInventory().setHelmet(helmet);
        }
    }

    /**
     * Manages drag-to-equip for shears (helmets) in inventory.
     */
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
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        ItemStack helmet = e.getCursor();
        ItemMeta meta = helmet.getItemMeta();

        int durability = ((Damageable) meta).getDamage();
        if (durability != 5 && durability != 10 && durability != 15 && durability != 20 && durability != 25) return;

        Sound sound = Sound.ITEM_ARMOR_EQUIP_GENERIC;
        switch (durability) {
            case 5:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Mage")) {
                    e.setCancelled(true);
                    return;
                }
                break;
            case 10:
                sound = Sound.ITEM_ARMOR_EQUIP_LEATHER;
                if (!className.equals("Rogue")) {
                    e.setCancelled(true);
                    return;
                }
                break;
            case 15:
                sound = Sound.ITEM_ARMOR_EQUIP_CHAIN;
                if (!className.equals("Archer")) {
                    e.setCancelled(true);
                    return;
                }
                break;
            case 20:
                sound = Sound.ITEM_ARMOR_EQUIP_GOLD;
                if (!className.equals("Cleric")) {
                    e.setCancelled(true);
                    return;
                }
                break;
            case 25:
                sound = Sound.ITEM_ARMOR_EQUIP_IRON;
                if (!className.equals("Warrior")) {
                    e.setCancelled(true);
                    return;
                }
                break;
        }

        e.setCancelled(true);
        pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
        if (pl.getInventory().getHelmet() == null) {
            ArmorEquipEvent newEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, null, helmet);
            Bukkit.getPluginManager().callEvent(newEvent);
            if (!newEvent.isCancelled()) {
                e.setCursor(new ItemStack(Material.AIR));
                pl.getInventory().setHelmet(helmet);
            }

        } else {
            ItemStack currentHelm = pl.getInventory().getHelmet();
            ArmorEquipEvent newEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, currentHelm, helmet);
            Bukkit.getPluginManager().callEvent(newEvent);
            if (!newEvent.isCancelled()) {
                e.setCursor(currentHelm);
                pl.getInventory().setHelmet(helmet);
            }
        }
    }

    @EventHandler
    public void onHelmetUnequip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;

        if (e.getCursor() != null && e.getCursor().getType() != Material.SHEARS
                && e.getCursor().getType() != Material.AIR) return;

        if (e.getSlot() != 39) return;
        Player pl = (Player) e.getWhoClicked();

        Bukkit.getPluginManager().callEvent(new ArmorEquipEvent
                (pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.HELMET, e.getCurrentItem(), e.getCursor()));
    }
}
