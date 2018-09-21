package me.skyfallin.plugin.events;

import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class ArtifactSlotEvent implements Listener {

    public HashMap<UUID, Long> artifactchangecd = new HashMap<UUID, Long>();
    public int artifactcdtime = 10;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent artifactevent) {

        Player player = (Player) artifactevent.getWhoClicked();
        UUID uuid = player.getUniqueId();
        int itemslot = artifactevent.getSlot();
        ItemStack cursoritem = player.getItemOnCursor();

        if (artifactevent.getClickedInventory() == null || player.getItemOnCursor() == null) return;
        if (itemslot == 0 && player.getGameMode() == GameMode.SURVIVAL && artifactevent.getClickedInventory().getType().equals(InventoryType.PLAYER) && cursoritem.getType() != Material.AIR) {
            if (!cursoritem.hasItemMeta() || !cursoritem.getItemMeta().hasLore()) {
                artifactevent.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
                player.sendMessage(ChatColor.RED + "Only artifacts are allowed in this slot.");
            } else if (cursoritem.hasItemMeta() && cursoritem.getItemMeta().hasLore()) {
                String loreAsString = ChatColor.stripColor(String.join(" ", player.getItemOnCursor().getItemMeta().getLore()));
                if (loreAsString.contains("Artifact")) {
                    if (!artifactchangecd.containsKey(uuid)) {
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
                        player.sendMessage(ChatColor.GREEN + "You equipped a new artifact!");
                        artifactchangecd.put(uuid, System.currentTimeMillis());
                    } else if (artifactchangecd.containsKey(uuid)) {
                        long secondsLeft = ((artifactchangecd.get(player.getUniqueId()) / 1000) + artifactcdtime) - (System.currentTimeMillis() / 1000);
                        if (secondsLeft > 0) {
                            artifactevent.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
                            player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.YELLOW + secondsLeft + ChatColor.RED + " seconds before changing your artifact again.");
                        } else if (secondsLeft <= 0) {
                            artifactchangecd.remove(uuid);
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
                            player.sendMessage(ChatColor.GREEN + "You equipped a new artifact!");
                            artifactchangecd.put(uuid, System.currentTimeMillis());
                        }
                    }
                }
                if (!loreAsString.contains("Artifact")) {
                    artifactevent.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
                    player.sendMessage(ChatColor.RED + "Only artifacts are allowed in this slot.");
                }
            }
        }
        if (itemslot == 0 && player.getGameMode() == GameMode.SURVIVAL && artifactevent.getClickedInventory().getType().equals(InventoryType.PLAYER) && cursoritem.getType() == Material.AIR) {
            artifactevent.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            IChatBaseComponent comp = IChatBaseComponent.ChatSerializer
                    .a("{\"text\":\"This is your §7artifact §eslot. For more info on §7artifacts§e, click\",\"color\":\"yellow\",\"extra\":[{\"text\":\" [here]§e.\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/artifactinfo\"}}]}");
            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }





    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        ItemStack item = swapevent.getMainHandItem();
        ItemStack offhand = swapevent.getOffHandItem();
        ItemMeta metaofnewoffhand = offhand.getItemMeta();
        ItemMeta metaofnewmainhand = item.getItemMeta();
        int slot = p.getInventory().getHeldItemSlot();

        if (slot == 0) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot. To replace your weapon, drag a new item to the slot.");
            return;
        }
    }

    @EventHandler
    public void onItemDrop (PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (!(droppedItem.hasItemMeta() && droppedItem.getItemMeta().hasLore())) return;

        String loreAsString = ChatColor.stripColor(String.join(" ", droppedItem.getItemMeta().getLore()));

        if (loreAsString.contains("Equipped") && loreAsString.contains("Artifact")
                && player.getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You cannot drop your equipped artifact. To replace your artifact, drag a new item to the slot.");
        }
    }
}
