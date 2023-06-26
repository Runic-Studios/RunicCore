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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;
    private static final Set<Integer> PLAYER_CRAFTING_SLOTS = new HashSet<>(Arrays.asList(1, 2, 3, 4));

    public PlayerMenuListener() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {

                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                InventoryView view = player.getOpenInventory();

                // If the open inventory is a player inventory
                // Update to the ring item
                // This will update even when it is closed, but
                // it is a small price to pay IMO
                if (isPlayerCraftingInv(view)) {

                    // uses packets to create visual items clientside that can't interact w/ the server
                    // prevents duping
                    PacketContainer packet1 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
                    packet1.getIntegers().write(0, 0); // Window ID
                    packet1.getIntegers().write(2, 1); // Slot ID
                    packet1.getItemModifier().write(0, profileIcon(player)); // Item

                    PacketContainer packet2 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
                    packet2.getIntegers().write(0, 0); // Window ID
                    packet2.getIntegers().write(2, 2); // Slot ID
                    packet2.getItemModifier().write(0, gemMenuIcon(player)); // Item

                    PacketContainer packet3 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
                    packet3.getIntegers().write(0, 0); // Window ID
                    packet3.getIntegers().write(2, 3); // Slot ID
                    packet3.getItemModifier().write(0, gatheringLevelItemStack(player)); // Item

                    PacketContainer packet4 = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
                    packet4.getIntegers().write(0, 0); // Window ID
                    packet4.getIntegers().write(2, 4); // Slot ID
                    packet4.getItemModifier().write(0, donorPerksIcon(player)); // Item

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet1);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet2);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet3);
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet4);
                }
            }
        }, 100L, 10L);
    }

    private static boolean isPlayerCraftingInv(InventoryView view) {
        return view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE;
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
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        if (isPlayerCraftingInv(view)) {
            clearPlayerCraftingSlots(view);
        }
    }
}