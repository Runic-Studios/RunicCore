package com.runicrealms.plugin.donator;

import com.runicrealms.plugin.RunicArtifacts;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ThreeD implements CommandExecutor {

    /**
     * Changes the player's artifact skin to be of the 3d variant
     */
    public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (RunicArtifacts.getThreeDManager().disguiseArtifact(player, player.getInventory().getItemInMainHand())) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                player.sendMessage(ChatColor.GREEN + "You have applied your weapon skin to your artifact!");
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                player.sendMessage(ChatColor.RED + "This item does not have any skins!");
            }
        } else {
            sender.sendMessage("You cannot run this command from console!");
        }
        return true;
    }
}