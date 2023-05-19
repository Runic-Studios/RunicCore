package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("runicteleport")
public class RunicTeleportCMD extends BaseCommand {

    public RunicTeleportCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("locations", context -> {
            Set<String> locations = new HashSet<>();
            for (CityLocation cityLocation : CityLocation.values()) {
                locations.add(cityLocation.getIdentifier());
            }
            for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
                locations.add(dungeonLocation.getIdentifier());
            }
            return locations;
        });
    }

    // runicteleport [player] [location]

    @Default
    @CatchUnknown
    @CommandCompletion("@players @locations")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: runicteleport [player] [location]");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        String locationString = args[1];
        Location location;
        if (CityLocation.getFromIdentifier(locationString) != CityLocation.TUTORIAL) { // the default is not null for this one
            location = CityLocation.getLocationFromIdentifier(locationString);
        } else if (DungeonLocation.getFromIdentifier(locationString) != null) {
            location = DungeonLocation.getLocationFromIdentifier(locationString);
        } else {
            commandSender.sendMessage(ChatColor.YELLOW + "Error, location not found");
            return;
        }
        assert location != null;
        player.teleport(location);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.PURPLE, 3));
    }
}