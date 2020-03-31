package com.runicrealms.plugin.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

    // todo: make player nameplates invisible w/ scoreboard teams?
    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player pl = e.getPlayer();
        pl.getInventory().clear();
        pl.setInvulnerable(true);
        pl.setMaxHealth(20);
        pl.setHealth(pl.getMaxHealth());
        pl.setHealthScale(20);
        pl.setLevel(0);
        pl.setExp(0);
        pl.setFoodLevel(20);
        pl.teleport(new Location(Bukkit.getWorld("Alterra"), -2318.5, 2, 1720.5));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadEvent e) {

        Player pl = e.getPlayer();
        pl.setInvulnerable(false);

        new BukkitRunnable() {
            @Override
            public void run() {

                // set their inventory
                pl.getInventory().setContents(e.getPlayerCache().getInventoryContents());
                pl.updateInventory();

                // set their hp to stored value from last logout
                int storedHealth = e.getPlayerCache().getCurrentHealth();

                // new players or corrupted data
                if (storedHealth == 0) {
                    storedHealth = HealthUtils.getBaseHealth();
                }

                HealthUtils.setPlayerMaxHealth(pl);
                HealthUtils.setHeartDisplay(pl);
                if (storedHealth <= pl.getMaxHealth()) {
                    pl.setHealth(storedHealth);
                } else {
                    pl.setHealth(pl.getMaxHealth());
                }

                // update player's level
                pl.setLevel(e.getPlayerCache().getClassLevel());
                int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel());
                int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel()+1);
                double proportion = (double) (e.getPlayerCache().getClassExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
                if (e.getPlayerCache().getClassLevel() >= PlayerLevelUtil.getMaxLevel()) pl.setExp(0);
                if (proportion < 0) proportion = 0.0f;
                pl.setExp((float) proportion);

                // set their location
                pl.teleport(e.getPlayerCache().getLocation());

                int version = RunicCore.getProtocolManager().getProtocolVersion(pl);
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + version);
                if (version < 472) { // less than 1.14
                    Bukkit.broadcastMessage("below 1.14");
                    pl.setResourcePack("https://www.dropbox.com/s/lzg9qlwrmlezvtz/RR%20Official%20Pack.zip?dl=1"); // 1.13.2 pack
                } else {
                    Bukkit.broadcastMessage("at or above 1.14");
                }


                PacketContainer packet = RunicCore.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
                packet.getModifier().writeDefaults();
                // declare type
                packet.getIntegers().write(0, (int) EntityType.SHEEP.getTypeId());
                //
                packet.getDoubles().write(0, pl.getLocation().getX());
                packet.getDoubles().write(1, pl.getLocation().getX());
                packet.getDoubles().write(2, pl.getLocation().getX());


                try {
                    RunicCore.getProtocolManager().sendServerPacket(pl, packet);
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

//    public void test() {
//        RunicCore.getProtocolManager().addPacketListener(new PacketAdapter(RunicCore.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.) {
//            @Override
//            public void onPacketReceiving(PacketEvent event) {
//                Player player = event.getPlayer();
//                PacketContainer packet = event.getPacket();
//                if (isMuted(player)) {
//                    System.out.println("[MUTED] " + player.getName() + ": " + packet.getStrings().read(0));
//                    event.setCancelled(true);
//                }
//            }
//        });
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFirstJoin(CharacterLoadEvent event) {

        Player pl = event.getPlayer();

        // setup for new players
        if (!pl.hasPlayedBefore()) {

            // broadcast new player welcome message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + pl.getName()
                    + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");

            // heal player
            HealthUtils.setPlayerMaxHealth(pl);
            HealthUtils.setHeartDisplay(pl);
            int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            pl.setHealth(playerHealth);
            pl.setFoodLevel(20);
        }
    }

    /**
     * Allows donator ranks to enter a full server
     */
    @EventHandler
    public void onJoinFullServer(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (e.getPlayer().hasPermission("core.full.join")) {
                e.allow();
            }
        }
    }
}
