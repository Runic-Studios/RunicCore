package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.event.ArmorEquipEvent;
import com.runicrealms.plugin.common.util.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

/**
 * Call our custom ArmorEquipEvent to listen for player gear changes
 * Most events run last to wait for other plugins
 * Handles off-hands now as well
 */
@SuppressWarnings("deprecation")
public class ArmorEquipListener implements Listener {
    private static final HashSet<Material> BLOCKED_MATERIALS = new HashSet<>() {{
        add(Material.CAMPFIRE);
        add(Material.FURNACE);
        add(Material.CHEST);
        add(Material.TRAPPED_CHEST);
        add(Material.BEACON);
        add(Material.DISPENSER);
        add(Material.DROPPER);
        add(Material.HOPPER);
        add(Material.LEGACY_WORKBENCH);
        add(Material.LEGACY_ENCHANTMENT_TABLE);
        add(Material.ENDER_CHEST);
        add(Material.ANVIL);
        add(Material.LEGACY_BED_BLOCK);
        add(Material.LEGACY_FENCE_GATE);
        add(Material.SPRUCE_FENCE_GATE);
        add(Material.BIRCH_FENCE_GATE);
        add(Material.ACACIA_FENCE_GATE);
        add(Material.JUNGLE_FENCE_GATE);
        add(Material.DARK_OAK_FENCE_GATE);
        add(Material.LEGACY_IRON_DOOR_BLOCK);
        add(Material.LEGACY_WOODEN_DOOR);
        add(Material.SPRUCE_DOOR);
        add(Material.BIRCH_DOOR);
        add(Material.JUNGLE_DOOR);
        add(Material.ACACIA_DOOR);
        add(Material.DARK_OAK_DOOR);
        add(Material.LEGACY_WOOD_BUTTON);
        add(Material.STONE_BUTTON);
        add(Material.LEGACY_TRAP_DOOR);
        add(Material.IRON_TRAPDOOR);
        add(Material.LEGACY_DIODE_BLOCK_OFF);
        add(Material.LEGACY_DIODE_BLOCK_ON);
        add(Material.LEGACY_REDSTONE_COMPARATOR_OFF);
        add(Material.LEGACY_REDSTONE_COMPARATOR_ON);
        add(Material.LEGACY_FENCE);
        add(Material.SPRUCE_FENCE);
        add(Material.BIRCH_FENCE);
        add(Material.JUNGLE_FENCE);
        add(Material.DARK_OAK_FENCE);
        add(Material.ACACIA_FENCE);
        add(Material.LEGACY_NETHER_FENCE);
        add(Material.BREWING_STAND);
        add(Material.CAULDRON);
        add(Material.LEGACY_SIGN_POST);
        add(Material.LEGACY_WALL_SIGN);
        add(Material.LEGACY_SIGN);
        add(Material.LEVER);
        add(Material.BLACK_SHULKER_BOX);
        add(Material.BLUE_SHULKER_BOX);
        add(Material.BROWN_SHULKER_BOX);
        add(Material.CYAN_SHULKER_BOX);
        add(Material.GRAY_SHULKER_BOX);
        add(Material.GREEN_SHULKER_BOX);
        add(Material.LIGHT_BLUE_SHULKER_BOX);
        add(Material.LIME_SHULKER_BOX);
        add(Material.MAGENTA_SHULKER_BOX);
        add(Material.ORANGE_SHULKER_BOX);
        add(Material.PINK_SHULKER_BOX);
        add(Material.PURPLE_SHULKER_BOX);
        add(Material.RED_SHULKER_BOX);
        add(Material.LEGACY_SILVER_SHULKER_BOX);
        add(Material.WHITE_SHULKER_BOX);
        add(Material.YELLOW_SHULKER_BOX);
    }};

    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

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
        if (e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER))
            return;
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
     * Fixes a bug that causes inventory to be out-of-sync (probably due to all our dupe or inventory checks)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArmorEquip(InventoryClickEvent e) {
        if (!(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT))
            return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> ((Player) e.getWhoClicked()).updateInventory());
    }

    /**
     * Handles right-clicking an armor piece to equip it
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.useItemInHand().equals(Result.DENY)) return;
        if (event.getAction() == Action.PHYSICAL) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;
        Player player = event.getPlayer();
        if (!event.useInteractedBlock().equals(Result.DENY)) {
            if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
                // Some blocks have actions when you right-click them which stop the client from equipping the armor in hand
                Material mat = event.getClickedBlock().getType();
                if (BLOCKED_MATERIALS.contains(mat)) return;
            }
        }
        ArmorType newArmorType = ArmorType.matchType(event.getItem());
        if (newArmorType == null) return;
        if (newArmorType.equals(ArmorType.HELMET)
                && isAirOrNull(event.getPlayer().getInventory().getHelmet())
                || newArmorType.equals(ArmorType.CHESTPLATE)
                && isAirOrNull(event.getPlayer().getInventory().getChestplate())
                || newArmorType.equals(ArmorType.LEGGINGS)
                && isAirOrNull(event.getPlayer().getInventory().getLeggings())
                || newArmorType.equals(ArmorType.BOOTS)
                && isAirOrNull(event.getPlayer().getInventory().getBoots())) {
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent
                    (
                            event.getPlayer(),
                            ArmorEquipEvent.EquipMethod.HOTBAR,
                            ArmorType.matchType(event.getItem()),
                            null,
                            event.getItem()
                    );
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

}
