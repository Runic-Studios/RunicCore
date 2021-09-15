package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.enums.ArmorType;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Call our custom ArmorEquipEvent to listen for player gear changes
 * Most events run last to wait for other plugins
 * Handles off-hands now as well
 */
public class ArmorEquipListener implements Listener {

    /**
     * Handles all methods of equipping armor from the inventory screen (shifting, etc.)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public final void inventoryClick(final InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (e.getAction() == InventoryAction.NOTHING) return;
        boolean shift = false, numberKey = false;
        if (e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)) {
            shift = true;
        }
        if (e.getClick().equals(ClickType.NUMBER_KEY)) {
            numberKey = true;
        }
        if (e.getSlotType() != SlotType.ARMOR && e.getSlotType() != SlotType.QUICKBAR && e.getSlotType() != SlotType.CONTAINER)
            return;
        if (e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (!e.getInventory().getType().equals(InventoryType.CRAFTING) && !e.getInventory().getType().equals(InventoryType.PLAYER))
            return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        ArmorType newArmorType = ArmorType.matchType(shift ? e.getCurrentItem() : e.getCursor());
        if (!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()) {
            // Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots slot.
            return;
        }
        if (shift) {
            newArmorType = ArmorType.matchType(e.getCurrentItem());
            if (newArmorType != null) {
                boolean equipping = e.getRawSlot() != newArmorType.getSlot();
                if (newArmorType.equals(ArmorType.HELMET)
                        && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getHelmet()))
                        || newArmorType.equals(ArmorType.CHESTPLATE)
                        && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getChestplate()))
                        || newArmorType.equals(ArmorType.LEGGINGS)
                        && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getLeggings()))
                        || newArmorType.equals(ArmorType.BOOTS)
                        && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getBoots()))) {
//                        || newArmorType.equals(ArmorType.OFFHAND)
//                        && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getItemInOffHand()))) {
                    ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent((Player) e.getWhoClicked(), ArmorEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        e.setCancelled(true);
                    }
                }
            }
        } else {
            ItemStack newArmorPiece = e.getCursor();
            ItemStack oldArmorPiece = e.getCurrentItem();
            if (numberKey) {
                if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {// Prevents shit in the 2by2 crafting
                    // e.getClickedInventory() == The players inventory
                    // e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
                    // e.getRawSlot() == The slot the item is going to.
                    // e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
                    ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                    if (!isAirOrNull(hotbarItem)) {// Equipping
                        newArmorType = ArmorType.matchType(hotbarItem);
                        newArmorPiece = hotbarItem;
                        oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
                    } else { // un-equipping
                        newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
                    }
                }
            } else {
                if (isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())) { // un-equip with no new item going into the slot.
                    newArmorType = ArmorType.matchType(e.getCurrentItem());
                }
                // e.getCurrentItem() == Unequip
                // e.getCursor() == Equip
                // newArmorType = ArmorType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
            }
            if (newArmorType != null && e.getRawSlot() == newArmorType.getSlot()) {
                ArmorEquipEvent.EquipMethod method = ArmorEquipEvent.EquipMethod.PICK_DROP;
                if (e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberKey)
                    method = ArmorEquipEvent.EquipMethod.HOTBAR_SWAP;
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent
                        (
                                (Player) e.getWhoClicked(),
                                method,
                                newArmorType,
                                oldArmorPiece,
                                newArmorPiece
                        );
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles right-clicking an armor piece to equip it
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (e.useItemInHand().equals(Result.DENY)) return;
        if (e.getAction() == Action.PHYSICAL) return;
        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = e.getPlayer();
        if (!e.useInteractedBlock().equals(Result.DENY)) {
            if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
                // some blocks have actions when you right-click them which stop the client from equipping the armor in hand
                Material mat = e.getClickedBlock().getType();
                for (Material blockedMaterial : blockedMaterials()) {
                    if (mat == blockedMaterial) return;
                }
            }
        }
        ArmorType newArmorType = ArmorType.matchType(e.getItem());
        if (newArmorType == null) return;
        if (newArmorType.equals(ArmorType.HELMET)
                && isAirOrNull(e.getPlayer().getInventory().getHelmet())
                || newArmorType.equals(ArmorType.CHESTPLATE)
                && isAirOrNull(e.getPlayer().getInventory().getChestplate())
                || newArmorType.equals(ArmorType.LEGGINGS)
                && isAirOrNull(e.getPlayer().getInventory().getLeggings())
                || newArmorType.equals(ArmorType.BOOTS)
                && isAirOrNull(e.getPlayer().getInventory().getBoots())) {
//                || newArmorType.equals(ArmorType.OFFHAND)
//                && isAirOrNull(e.getPlayer().getInventory().getItemInOffHand())) {
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent
                    (
                            e.getPlayer(),
                            ArmorEquipEvent.EquipMethod.HOTBAR,
                            ArmorType.matchType(e.getItem()),
                            null,
                            e.getItem()
                    );
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                e.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    /**
     * Handles clicking and dragging items to equip them
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventoryDrag(InventoryDragEvent event) {
        if (event.getRawSlots().isEmpty()) return;
        /*
        getType() seems to always be even.
        old Cursor gives the item you are equipping
        raw slot is the ArmorType slot
        can't replace armor using this method making getCursor() useless.
         */
        ArmorType type = ArmorType.matchType(event.getOldCursor());
        if (type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)) {
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent
                    (
                            (Player) event.getWhoClicked(),
                            ArmorEquipEvent.EquipMethod.DRAG,
                            type,
                            null,
                            event.getOldCursor()
                    );
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                event.setResult(Result.DENY);
                event.setCancelled(true);
            }
        }
    }

    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    @SuppressWarnings("deprecation")
    private Set<Material> blockedMaterials() {
        Set<Material> blockedMaterials = new HashSet<>();
        blockedMaterials.add(Material.FURNACE);
        blockedMaterials.add(Material.CHEST);
        blockedMaterials.add(Material.TRAPPED_CHEST);
        blockedMaterials.add(Material.BEACON);
        blockedMaterials.add(Material.DISPENSER);
        blockedMaterials.add(Material.DROPPER);
        blockedMaterials.add(Material.HOPPER);
        blockedMaterials.add(Material.LEGACY_WORKBENCH);
        blockedMaterials.add(Material.LEGACY_ENCHANTMENT_TABLE);
        blockedMaterials.add(Material.ENDER_CHEST);
        blockedMaterials.add(Material.ANVIL);
        blockedMaterials.add(Material.LEGACY_BED_BLOCK);
        blockedMaterials.add(Material.LEGACY_FENCE_GATE);
        blockedMaterials.add(Material.SPRUCE_FENCE_GATE);
        blockedMaterials.add(Material.BIRCH_FENCE_GATE);
        blockedMaterials.add(Material.ACACIA_FENCE_GATE);
        blockedMaterials.add(Material.JUNGLE_FENCE_GATE);
        blockedMaterials.add(Material.DARK_OAK_FENCE_GATE);
        blockedMaterials.add(Material.LEGACY_IRON_DOOR_BLOCK);
        blockedMaterials.add(Material.LEGACY_WOODEN_DOOR);
        blockedMaterials.add(Material.SPRUCE_DOOR);
        blockedMaterials.add(Material.BIRCH_DOOR);
        blockedMaterials.add(Material.JUNGLE_DOOR);
        blockedMaterials.add(Material.ACACIA_DOOR);
        blockedMaterials.add(Material.DARK_OAK_DOOR);
        blockedMaterials.add(Material.LEGACY_WOOD_BUTTON);
        blockedMaterials.add(Material.STONE_BUTTON);
        blockedMaterials.add(Material.LEGACY_TRAP_DOOR);
        blockedMaterials.add(Material.IRON_TRAPDOOR);
        blockedMaterials.add(Material.LEGACY_DIODE_BLOCK_OFF);
        blockedMaterials.add(Material.LEGACY_DIODE_BLOCK_ON);
        blockedMaterials.add(Material.LEGACY_REDSTONE_COMPARATOR_OFF);
        blockedMaterials.add(Material.LEGACY_REDSTONE_COMPARATOR_ON);
        blockedMaterials.add(Material.LEGACY_FENCE);
        blockedMaterials.add(Material.SPRUCE_FENCE);
        blockedMaterials.add(Material.BIRCH_FENCE);
        blockedMaterials.add(Material.JUNGLE_FENCE);
        blockedMaterials.add(Material.DARK_OAK_FENCE);
        blockedMaterials.add(Material.ACACIA_FENCE);
        blockedMaterials.add(Material.LEGACY_NETHER_FENCE);
        blockedMaterials.add(Material.BREWING_STAND);
        blockedMaterials.add(Material.CAULDRON);
        blockedMaterials.add(Material.LEGACY_SIGN_POST);
        blockedMaterials.add(Material.LEGACY_WALL_SIGN);
        blockedMaterials.add(Material.LEGACY_SIGN);
        blockedMaterials.add(Material.LEVER);
        blockedMaterials.add(Material.BLACK_SHULKER_BOX);
        blockedMaterials.add(Material.BLUE_SHULKER_BOX);
        blockedMaterials.add(Material.BROWN_SHULKER_BOX);
        blockedMaterials.add(Material.CYAN_SHULKER_BOX);
        blockedMaterials.add(Material.GRAY_SHULKER_BOX);
        blockedMaterials.add(Material.GREEN_SHULKER_BOX);
        blockedMaterials.add(Material.LIGHT_BLUE_SHULKER_BOX);
        blockedMaterials.add(Material.LIME_SHULKER_BOX);
        blockedMaterials.add(Material.MAGENTA_SHULKER_BOX);
        blockedMaterials.add(Material.ORANGE_SHULKER_BOX);
        blockedMaterials.add(Material.PINK_SHULKER_BOX);
        blockedMaterials.add(Material.PURPLE_SHULKER_BOX);
        blockedMaterials.add(Material.RED_SHULKER_BOX);
        blockedMaterials.add(Material.LEGACY_SILVER_SHULKER_BOX);
        blockedMaterials.add(Material.WHITE_SHULKER_BOX);
        blockedMaterials.add(Material.YELLOW_SHULKER_BOX);
        return blockedMaterials;
    }
}
