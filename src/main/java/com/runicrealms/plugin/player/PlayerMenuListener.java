package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.mysterybox.MysteryLoot;
import com.runicrealms.plugin.mysterybox.animation.Animation;
import com.runicrealms.plugin.mysterybox.animation.animations.Tornado;
import com.runicrealms.plugin.utilities.ColorUtil;
import net.minecraft.server.v1_13_R2.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Controls the player menu in the inventory crafting slots
 */
public class PlayerMenuListener implements Listener {

    private static final int PLAYER_CRAFT_INV_SIZE = 5;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Player pl = e.getPlayer();

        // item 1
        // todo: fix
        ItemStack plMenu = item(pl, Material.PLAYER_HEAD, "&eCharacter Info",
                "\n&7Title: &aNone" +
                        "\n&7Item Drop Chance: &f" + (pl.getLevel()/2) + "&7%");

        //item 2
        ItemStack questJournal = item(pl, Material.WRITABLE_BOOK, "&6Quest Journal",
                "\n&fClick here &7to view\n&7the quest journal!");

        ItemStack lootChests = item(pl, Material.CHEST, "&dMystery Boxes",
                "\n&aFeature Coming Soon!");

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {

            // item 3 must update dynamically
            String healthBonus = statBoost(
                    (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() -PlayerLevelListener.getHpAtLevel(pl));
            String manaBoost = statBoost(GearScanner.getManaBoost(pl));
            String attackDamage = statBoost(GearScanner.getAttackDamage(pl));
            String healingBoost = statBoost(GearScanner.getHealingBoost(pl));
            String magicBoost = statBoost(GearScanner.getMagicBoost(pl));

            ItemStack gemMenu = item(pl, Material.REDSTONE, "&aCharacter Stats",
                    "\n&7Total &c❤ &7bonus: " + healthBonus +
                            "\n&7Total &3✸ &7bonus: " + manaBoost +
                            "\n&7Total &c⚔ &7bonus: " + attackDamage +
                            "\n&7Total &a✦ &7bonus: " + healingBoost +
                            "\n&7Total &3ʔ &7bonus: " + magicBoost);

            InventoryView view = pl.getOpenInventory();

            // If the open inventory is a player inventory
            // Update to the ring item
            // This will update even when it is closed, but
            // it is a small price to pay IMO
            if (isPlayerCraftingInv(view)) {

                // uses packets to create visual items clientside that can't interact w/ the server
                // prevents duping
                PacketPlayOutSetSlot packet1 = new PacketPlayOutSetSlot(0, 1, CraftItemStack.asNMSCopy(plMenu));
                PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(0, 2, CraftItemStack.asNMSCopy(questJournal));
                PacketPlayOutSetSlot packet3 = new PacketPlayOutSetSlot(0, 3, CraftItemStack.asNMSCopy(gemMenu));
                PacketPlayOutSetSlot packet4 = new PacketPlayOutSetSlot(0, 4, CraftItemStack.asNMSCopy(lootChests));

                ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet1);
                ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet2);
                ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet3);
                ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet4);

            }
        }, 0L, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        InventoryView view = event.getView();

        // Remove the ring item in the matrix to prevent
        // players from duping them
        if (isPlayerCraftingInv(view)) {

            view.setItem(1, null);
            view.setItem(2, null);
            view.setItem(3, null);
            view.setItem(4, null);

            view.getTopInventory().clear();
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryView view = event.getView();

        // Don't allow players to remove anything from their
        // own crafting matrix
        // The view includes the player's entire inventory
        // as well, so check to make sure that the clicker
        // did not click on their own inventory
        if (isPlayerCraftingInv(view) &&
                event.getClickedInventory() != event.getWhoClicked().getInventory()) {
            if (event.getSlot() < 5 && event.getSlot() > 0) {

                event.setCancelled(true);
                Player pl = (Player) event.getWhoClicked();
                pl.updateInventory();

                if (event.getSlot() == 2 && pl.getGameMode() != GameMode.CREATIVE) {
                    pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                    pl.performCommand("quest");
                } else if (event.getSlot() == 4) {
                    Animation animation = new Tornado(MysteryLoot.getMysteryItems());
                    animation.spawn(pl, pl.getLocation());
                }
            }
        }

        // prevent question marks from dropping out of quest journal
        if (event.getInventory().getTitle().toLowerCase().contains("quest journal")) {
            event.setCancelled(true);
        }
    }

    private static boolean isPlayerCraftingInv(InventoryView view) {
        return view.getTopInventory().getSize() == PLAYER_CRAFT_INV_SIZE;
    }

    private String statBoost(int stat) {
        if (stat > 0) {
            return "&a+" + stat;
        } else {
            return  "&f+" + stat;
        }
    }

    private ItemStack item(Player pl, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (material == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(pl);
        }

        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ColorUtil.format(name));
        String[] desc = description.split("\n");
        for (String line : desc) {
            lore.add(ColorUtil.format(line));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}