package us.fortherealm.plugin.events;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class HearthstoneEvent implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();
        if (itemslot == 8 && p.getGameMode() == GameMode.SURVIVAL && e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            IChatBaseComponent comp = IChatBaseComponent.ChatSerializer
                    .a("{\"text\":\"This is your §dhearthstone§e. " +
                            "For more info, click\",\"color\":\"yellow\",\"extra\":[{\"text\":\" [here]§e.\"," +
                            "\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/hearthstone\"}}]}");
            PacketPlayOutChat packet = new PacketPlayOutChat(comp);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            return;
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
        if (slot == 8) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot.");
            return;
        }
    }

    @EventHandler
    public void onQuit (PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ItemStack hearthstone = new ItemStack(Material.NETHER_STAR);
        ItemMeta hsmeta = hearthstone.getItemMeta();
        hsmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Hearthstone");
        ArrayList<String> hslore = new ArrayList<String>();
        hslore.add("§7");
        hslore.add(ChatColor.GRAY + "Right-click: " + ChatColor.WHITE + "Return to " + ChatColor.GREEN + "the tutorial");
        hslore.add("§7");
        hslore.add(ChatColor.GRAY + "Speak to an innkeeper to change your home location.");
        hsmeta.setLore(hslore);
        hearthstone.setItemMeta(hsmeta);
        player.getInventory().setItem(8, hearthstone);
    }
}

