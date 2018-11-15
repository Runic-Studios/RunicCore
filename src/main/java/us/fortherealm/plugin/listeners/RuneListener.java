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

public class RuneListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent runeevent) {

        Player player = (Player) runeevent.getWhoClicked();
        int itemslot = runeevent.getSlot();

        // disable players from interacting with rune slot
        if (itemslot == 1 && player.getGameMode() == GameMode.SURVIVAL && runeevent.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            runeevent.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            IChatBaseComponent comp = IChatBaseComponent.ChatSerializer
                    .a("{\"text\":\"This is your §7rune §eslot. For more info on §7runes§e, click\",\"color\":\"yellow\",\"extra\":[{\"text\":\" [HERE]\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/runeinfo\"}}]}");
            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        // block rune swapping
        if (slot == 1) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot.");
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {

        Player player = e.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        // cancel rune dropping
        if (slot == 1 && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You cannot drop your rune.");
        }
    }
}

