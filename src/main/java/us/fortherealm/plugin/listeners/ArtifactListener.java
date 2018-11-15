package us.fortherealm.plugin.listeners;

import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
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

public class ArtifactListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent artifactevent) {

        Player player = (Player) artifactevent.getWhoClicked();
        int itemslot = artifactevent.getSlot();

        // disable players from interacting with artifact slot
        if (itemslot == 0 && player.getGameMode() == GameMode.SURVIVAL && artifactevent.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            artifactevent.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            IChatBaseComponent comp = IChatBaseComponent.ChatSerializer
                    .a("{\"text\":\"This is your §7artifact §eslot. For more info on §7artifacts§e, click\",\"color\":\"yellow\",\"extra\":[{\"text\":\" [HERE]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/artifactinfo\"}}]}");
            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        // block artifact swapping
        if (slot == 0) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot.");
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {

        Player player = e.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        // cancel artifact dropping
        if (slot == 0 && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You cannot drop your artifact.");
        }
    }
}
