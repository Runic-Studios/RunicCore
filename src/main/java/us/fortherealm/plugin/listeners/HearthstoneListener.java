package us.fortherealm.plugin.listeners;

import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class HearthstoneListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // if its the 3rd slot in a player's inventory, run the stuff
        if (itemslot == 2 && p.getGameMode() == GameMode.SURVIVAL && e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            IChatBaseComponent comp = IChatBaseComponent.ChatSerializer
                    .a("{\"text\":\"This is your §dhearthstone§e. " +
                            "For more info, click\",\"color\":\"yellow\",\"extra\":[{\"text\":\" [here]§e.\"," +
                            "\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/hearthstone\"}}]}");
            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        // cancel the event
        if (slot == 2) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot.");
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {

        Player player = e.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        // cancel hearthstone dropping
        if (slot == 2 && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You cannot drop your hearthstone.");
        }
    }
}

