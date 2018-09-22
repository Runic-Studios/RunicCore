package us.fortherealm.plugin.events;

import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class FirstJoinEvent implements Listener {

    private Main plugin = Main.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                player.sendMessage(ChatColor.GREEN + "Welcome back to the project, " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + ".");
            }
        }, 1);


        if (!player.hasPlayedBefore()) {
            ItemStack sparringsword = new ItemStack(Material.WOOD_SWORD);
            ItemMeta swordmeta = sparringsword.getItemMeta();
            swordmeta.setDisplayName(ChatColor.WHITE + "Blunt Sparring Sword");
            ArrayList<String> swordlore = new ArrayList<String>();
            swordlore.add(ChatColor.GRAY + "Skill: " + ChatColor.RED + "Speed");
            // swordlore.add(ChatColor.GRAY + "Sword");
            swordlore.add(ChatColor.YELLOW + "Artifact");
            swordlore.add(ChatColor.DARK_GRAY + "Equipped");
            swordmeta.setLore(swordlore);
            swordmeta.setUnbreakable(true);
            swordmeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            swordmeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            sparringsword.setItemMeta(swordmeta);

            ItemStack firerune = new ItemStack(Material.INK_SACK, 1, DyeColor.ORANGE.getDyeData());
            ItemMeta runemeta = firerune.getItemMeta();
            runemeta.setDisplayName(ChatColor.WHITE + "Smoldering Rune");
            ArrayList<String> runelore = new ArrayList<String>();
            runelore.add(ChatColor.GRAY + "Skill: " + ChatColor.RED + "Fireball");
            runelore.add(ChatColor.YELLOW + "Rune");
            runelore.add(ChatColor.DARK_GRAY + "Equipped");
            runemeta.setLore(runelore);
            firerune.setItemMeta(runemeta);

            ItemStack hearthstone = new ItemStack(Material.NETHER_STAR);
            ItemMeta hsmeta = hearthstone.getItemMeta();
            hsmeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Hearthstone");
            ArrayList<String> hslore = new ArrayList<String>();
            hslore.add("§7");
            hslore.add(ChatColor.GRAY + "Right-click: " + ChatColor.WHITE + "Return to " + ChatColor.GREEN + "the tutorial");
            hslore.add("§7");
            hslore.add(ChatColor.GRAY + "Speak to an §einnkeeper §7to change your home location.");
            hsmeta.setLore(hslore);
            hearthstone.setItemMeta(hsmeta);

            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " joined the server for the first time!");
            //player.setHealthScale(4.0);//2 hearts
            //player.setHealth(player.getMaxHealth());//default hp
            player.getInventory().setItem(0, sparringsword);
            player.getInventory().setItem(1, firerune);
            player.getInventory().setItem(8, hearthstone);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    player.setHealthScale((player.getMaxHealth() / 12.5));//ex: (50 / 12.5) = 4.0 = 2 hearts
                    player.setHealth(player.getMaxHealth());//default hp
                }
            }, 18); // ~ 1 second, shorter than 20 ticks so as to let the scoreboard update.
        }
    }
}
