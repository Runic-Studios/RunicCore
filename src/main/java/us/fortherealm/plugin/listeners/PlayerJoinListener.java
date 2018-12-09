package us.fortherealm.plugin.listeners;

import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;

public class PlayerJoinListener implements Listener {

    private Main plugin = Main.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // set join message
        // TODO: inform players if their guild mate or friend logs in.
        event.setJoinMessage("");
        player.sendMessage(ChatColor.GRAY + "Loading resource pack...");

        if (!player.hasPlayedBefore()) {

            // broadcast new player welcome message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.LIGHT_PURPLE + " joined the server for the first time!");

            // set the player's class to "None"
            Main.getInstance().getConfig().set(player.getUniqueId() + ".info.class", "None");
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();

            // create the hearthstone
            ItemStack hearthstone = new ItemStack(Material.NETHER_STAR);
            ItemMeta hsmeta = hearthstone.getItemMeta();
            hsmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Hearthstone");
            ArrayList<String> hslore = new ArrayList<String>();
            // TODO: add variable for HS location
            hslore.add(ChatColor.GRAY + "Left-click: Return to your " + ChatColor.GOLD + "Guild Hall");
            hslore.add(ChatColor.GRAY + "Right-click: Return to " + ChatColor.GREEN + "The Tutorial");
            hslore.add(ChatColor.DARK_GRAY
                    + "Speak to an " + ChatColor.YELLOW + "innkeeper "
                    + ChatColor.DARK_GRAY + "to change your home.");
            hsmeta.setLore(hslore);
            hearthstone.setItemMeta(hsmeta);

            // set the item!
            player.getInventory().setItem(2, hearthstone);

            // update the player's health scale and hp, delayed by 10 ticks to wait for their max health to be updated
            new BukkitRunnable() {
                @Override
                public void run() {
                    // ex: (50 / 12.5) = 4.0 = 2 hearts
                    player.setHealthScale((player.getMaxHealth() / 12.5));
                    // make sure player is at full health
                    player.setHealth(player.getMaxHealth());
                }
            }.runTaskLater(plugin, 10);
        }
    }
}
