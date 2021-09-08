package com.runicrealms.plugin.player.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.events.OffhandEquipEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents off-hands from being clicked or dragged into the inventory
 */
public class OffhandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // executes FIRST
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        ItemStack item = event.getCurrentItem() == null ? event.getCursor() : event.getCurrentItem();

        if (item == null) return;

        if (event.getSlot() == 40 || event.getClick() == ClickType.SWAP_OFFHAND) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
                player.sendMessage(ChatColor.GRAY + "To equip an offhand, hold it and right-click!");
            } else {
                player.sendMessage(ChatColor.GRAY + "To remove your offhand, hold it and right-click!");
            }
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!event.getInventory().getType().equals(InventoryType.CRAFTING)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (event.getInventorySlots().contains(40)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getHand() == EquipmentSlot.HAND || (event.getHand() == EquipmentSlot.OFF_HAND && event.getAction() == Action.RIGHT_CLICK_AIR)) {

            ItemStack mainHand = event.getPlayer().getInventory().getItemInMainHand();
            ItemStack offHand = event.getPlayer().getInventory().getItemInOffHand();

            if (mainHand.getType() != Material.AIR && offHand.getType() == Material.AIR) { // main -> off (empty)
                if (RunicItemsAPI.getItemStackTemplate(mainHand) instanceof RunicItemOffhandTemplate) {
                    OffhandEquipEvent customEvent = new OffhandEquipEvent(event.getPlayer(), mainHand);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    if (!customEvent.isCancelled()) {
                        event.getPlayer().getInventory().setItemInOffHand(mainHand);
                        event.getPlayer().getInventory().setItemInMainHand(null);
                    }
                }
            } else if (mainHand.getType() == Material.AIR && offHand.getType() != Material.AIR) { // off -> main (empty)
                if (RunicItemsAPI.getItemStackTemplate(offHand) instanceof RunicItemOffhandTemplate) {
                    OffhandEquipEvent customEvent = new OffhandEquipEvent(event.getPlayer(), null);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    if (!customEvent.isCancelled()) {
                        event.getPlayer().getInventory().setItemInMainHand(offHand);
                        event.getPlayer().getInventory().setItemInOffHand(null);
                    }
                }
            } else if (mainHand.getType() != Material.AIR && offHand.getType() != Material.AIR) { // both filled
                if (RunicItemsAPI.getItemStackTemplate(mainHand) instanceof RunicItemOffhandTemplate) { // swap offhands
                    OffhandEquipEvent customEvent = new OffhandEquipEvent(event.getPlayer(), mainHand);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    if (!customEvent.isCancelled()) {
                        event.getPlayer().getInventory().setItemInMainHand(offHand);
                        event.getPlayer().getInventory().setItemInOffHand(mainHand);
                    }
                } else if (RunicItemsAPI.getItemStackTemplate(offHand) instanceof RunicItemOffhandTemplate) { // attempted removal of offhand but main is full
                    event.getPlayer().sendMessage(ChatColor.GRAY + "To remove your offhand, clear items from your main hand then right-click!");
                }
            }
        }
    }

}
