package com.runicrealms.plugin.player.bar;

import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * This class is used to display use information about a player over their head
 *
 * @author Skyfallin
 */
public class PlayerBar {
    private final Player player;
    private ArmorStand armorStand;
    private Parrot parrot;

    public PlayerBar(Player player) {
        this.player = player;
        this.parrot = setupParrot();
        this.armorStand = setupArmorStand();
    }

    public void attachPlayerBar(Map<Player, PlayerBar> playerBars) {
        // Store them in the HashMaps
        playerBars.put(player, this);
        // Mount the Armor Stand to the Parrot
        parrot.addPassenger(armorStand);
        // Mount the Parrot to the Player
        player.addPassenger(parrot);
    }

    public void clearPlayerBar() {
        this.armorStand.remove();
        this.parrot.remove();
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Player getPlayer() {
        return player;
    }

    public void refreshPlayerBar(Map<Player, PlayerBar> playerBars) {
        this.parrot = setupParrot();
        this.armorStand = setupArmorStand();
        attachPlayerBar(playerBars);
    }

    private ArmorStand setupArmorStand() {
        // Create the Armor Stand
        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName((int) player.getHealth() + "" + ChatColor.RED + " ❤");
        armorStand.setMarker(true);
        return armorStand;
    }

    private Parrot setupParrot() {
        // Create the Parrot
        Parrot parrot = (Parrot) player.getWorld().spawnEntity(player.getLocation(), EntityType.PARROT);
        parrot.setAI(false);
        parrot.setSilent(true);
        parrot.setInvisible(true);
        parrot.setInvulnerable(true);
        parrot.setSitting(false);
        return parrot;
    }
}
