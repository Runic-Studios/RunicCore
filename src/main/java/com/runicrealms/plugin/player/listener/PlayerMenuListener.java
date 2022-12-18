package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.StatsGUI;
import com.runicrealms.plugin.utilities.ColorUtil;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;
    private static final Set<Integer> PLAYER_CRAFTING_SLOTS = new HashSet<>(Arrays.asList(1, 2, 3, 4));

    public PlayerMenuListener() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {

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
                    PacketPlayOutSetSlot packet1 = new PacketPlayOutSetSlot(0, 1, CraftItemStack.asNMSCopy(achievementsIcon(player)));
                    PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(0, 2, CraftItemStack.asNMSCopy(gemMenuIcon(player)));
                    PacketPlayOutSetSlot packet3 = new PacketPlayOutSetSlot(0, 3, CraftItemStack.asNMSCopy(gatheringLevelItemStack(player)));
                    PacketPlayOutSetSlot packet4 = new PacketPlayOutSetSlot(0, 4, CraftItemStack.asNMSCopy(groupFinderIcon(player)));
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet1);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet2);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet3);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet4);
                }
            }
        }, 100L, 10L);
    }

    private static boolean isPlayerCraftingInv(InventoryView view) {
        return view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE;
    }

    /**
     * Creates the menu icon for the
     *
     * @param player who the menu belongs to
     * @return a visual menu item for settings
     */
    private ItemStack achievementsIcon(Player player) {
        return item
                (
                        player,
                        Material.PLAYER_HEAD,
                        "&e" + player.getName() + "'s Achievements",
                        "\n&6&lCLICK" + "\n&7To view your achievements!"
                );
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
                        "\n&6&lCLICK" + "\n&7To view your gathering skills!" + "\n&7They are account-wide!"
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
                        "\n&6&lCLICK" + "\n&7To view your character stats!"
                );
    }

    /**
     * The info item for the player to find a group
     *
     * @param player to display menu for
     * @return an Itemstack to display
     */
    private ItemStack groupFinderIcon(Player player) {
        return item
                (
                        player,
                        Material.BELL,
                        ChatColor.YELLOW + "Group Finder",
                        "\n&6&lCLICK" + "\n&7To open the Group Finder!"
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
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
        if (!PLAYER_CRAFTING_SLOTS.contains(event.getSlot())) return;
        event.setCancelled(true);
        player.updateInventory();
        if (event.getCursor() == null) return;
        if (event.getCursor().getType() != Material.AIR) return; // prevents clicking with items on cursor
        if (event.getSlot() == 2) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            player.openInventory(new StatsGUI(player).getInventory());
        } else if (event.getSlot() == 4) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            Bukkit.dispatchCommand(player, "group");
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
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (event.getInventory().equals(event.getView().getBottomInventory())) return;
        if (event.getInventorySlots().contains(1) || event.getInventorySlots().contains(2)
                || event.getInventorySlots().contains(3) || event.getInventorySlots().contains(4)) {
            event.setCancelled(true);
        }
    }
}