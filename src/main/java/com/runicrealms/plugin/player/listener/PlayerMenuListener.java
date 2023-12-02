package com.runicrealms.plugin.player.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.donor.ui.DonorUI;
import com.runicrealms.plugin.player.ui.ProfileUI;
import com.runicrealms.plugin.player.ui.StatsGUI;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;
    private static final Set<Integer> PLAYER_CRAFTING_SLOTS = new HashSet<>(Arrays.asList(1, 2, 3, 4));

    // Cache the packets so that we don't have to reconstruct them
    private static final Map<UUID, Set<PacketContainer>> craftingSlotPackets = new HashMap<>();

    public PlayerMenuListener() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {

                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                InventoryView view = player.getOpenInventory();

                if (isPlayerCraftingInv(view)) {
                    Set<PacketContainer> packets = craftingSlotPackets.get(uuid);
                    if (packets == null) continue;
                    for (PacketContainer packet : packets) {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                    }
                }
            }
        }, 100L, 10L);
    }

    private static boolean isPlayerCraftingInv(InventoryView view) {
        return view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE;
    }

    private static PacketContainer constructCraftingSlotPacket(int slot, ItemStack item) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
        packet.getIntegers().write(0, 0); // Window ID
        packet.getIntegers().write(2, slot); // Slot ID
        packet.getItemModifier().write(0, item); // Item
        return packet;
    }

    /**
     * Creates the menu icon for the profile
     *
     * @param player who the menu belongs to
     * @return a visual menu item for profile
     */
    private ItemStack profileIcon(Player player) {
        return item
                (
                        player,
                        Material.PLAYER_HEAD,
                        "&e" + player.getName() + "'s Profile",
                        """

                                &6&lCLICK
                                &7To view your settings
                                &7and achievements!"""
                );
    }

    private void clearPlayerCraftingSlots(InventoryView view) {
        view.setItem(0, null);
        view.setItem(1, null);
        view.setItem(2, null);
        view.setItem(3, null);
        view.setItem(4, null);
    }

    /**
     * The info item for the player's gathering levels
     *
     * @param player to display menu for
     * @return an Itemstack to display
     */
    private ItemStack gatheringLevelItemStack(Player player) {
        return item
                (
                        player,
                        Material.IRON_PICKAXE,
                        "&eGathering Skills",
                        """

                                &6&lCLICK
                                &7To view your gathering skills!
                                &7They are account-wide!"""
                );
    }

    /**
     * Creates the menu icon for the
     *
     * @param player who the menu belongs to
     * @return a visual menu item for gems
     */
    private ItemStack gemMenuIcon(Player player) {
        return item
                (
                        player,
                        Material.REDSTONE,
                        "&eCharacter Stats",
                        """

                                &6&lCLICK
                                &7To view your character stats!"""
                );
    }

    /**
     * The info item for the donor perks icon
     *
     * @param player to display menu for
     * @return an ItemStack to display
     */
    private ItemStack donorPerksIcon(Player player) {
        return item
                (
                        player,
                        Material.EXPERIENCE_BOTTLE,
                        ChatColor.YELLOW + "Donor Perks",
                        """

                                &6&lCLICK
                                &7To view and activate donor perks!"""
                );
    }

    /**
     * The info item for the mount menu icon
     *
     * @param player to display menu for
     * @return an ItemStack to display
     */
    private ItemStack mountMenuIcon(Player player) {
        return item
                (
                        player,
                        Material.SADDLE,
                        ChatColor.YELLOW + "Mount Menu",
                        """

                                &6&lCLICK
                                &7To view your mount appearances
                                &7and riding achievements!"""
                );
    }

    private ItemStack item(Player player, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(player);
        }

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ColorUtil.format(name));
        String[] desc = description.split("\n");
        for (String line : desc) {
            lore.add(ColorUtil.format(line));
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(5);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.CRAFTING) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
        if (!PLAYER_CRAFTING_SLOTS.contains(event.getSlot())) return;
        event.setCancelled(true);
        player.updateInventory();
        if (event.getCursor() == null) return;
        if (event.getCursor().getType() != Material.AIR)
            return; // prevents clicking with items on cursor
        if (event.getSlot() == 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            player.openInventory((new ProfileUI(player)).getInventory());
        } else if (event.getSlot() == 2) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            player.openInventory(new StatsGUI(player).getInventory());
        } else if (event.getSlot() == 4) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            player.openInventory(new DonorUI(player).getInventory());
        }
        //the mount menu logic is handled by the RunicMounts plugin
    }

    /**
     * Remove the items from the crafting matrix
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        if (!isPlayerCraftingInv(view)) return;
        view.setItem(1, null);
        view.setItem(2, null);
        view.setItem(3, null);
        view.setItem(4, null);
        view.getTopInventory().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!event.getInventory().getType().equals(InventoryType.CRAFTING)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (event.getInventory().equals(event.getView().getBottomInventory())) return;
        if (event.getInventorySlots().contains(1) || event.getInventorySlots().contains(2)
                || event.getInventorySlots().contains(3) || event.getInventorySlots().contains(4)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        if (isPlayerCraftingInv(view)) {
            clearPlayerCraftingSlots(view);
        }
        // Cache the items in their crafting menu
        Set<PacketContainer> packets = new HashSet<>();
        packets.add(constructCraftingSlotPacket(0, mountMenuIcon(event.getPlayer())));
        packets.add(constructCraftingSlotPacket(1, profileIcon(event.getPlayer())));
        packets.add(constructCraftingSlotPacket(2, gemMenuIcon(event.getPlayer())));
        packets.add(constructCraftingSlotPacket(3, gatheringLevelItemStack(event.getPlayer())));
        packets.add(constructCraftingSlotPacket(4, donorPerksIcon(event.getPlayer())));

        craftingSlotPackets.put(event.getPlayer().getUniqueId(), packets);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        if (isPlayerCraftingInv(view)) {
            clearPlayerCraftingSlots(view);
        }
        craftingSlotPackets.remove(event.getPlayer().getUniqueId());
    }

}