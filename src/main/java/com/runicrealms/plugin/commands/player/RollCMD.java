package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
@CommandAlias("roll")
public class RollCMD extends BaseCommand {
    private static final int COOLDOWN = 5;
    private final Random random = new Random();
    private final Set<UUID> cooldownPlayers = new HashSet<>();

    @CatchUnknown
    @Default
    public void onCommandClass(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Only a player may run this command!");
            return;
        }
        roll(player);
    }

    public void roll(Player player) {
        if (cooldownPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Command on cooldown!");
            return;
        }
        int result = random.nextInt(100) + 1;  // Generate a random integer between 1 and 100
        for (Entity nearby : player.getWorld().getNearbyEntities(player.getLocation(), 50, 50, 50, target -> target instanceof Player)) {
            nearby.sendMessage(ChatColor.YELLOW + player.getName() + " rolled a " + ChatColor.WHITE + result + ChatColor.YELLOW + "!");
        }
        cooldownPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> cooldownPlayers.remove(player.getUniqueId()), COOLDOWN * 20L);
    }
}
