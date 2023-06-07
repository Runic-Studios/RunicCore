package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.TravelLocation;
import com.runicrealms.plugin.TravelType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import static com.runicrealms.plugin.TravelLocation.fastTravelTask;

@CommandAlias("travel")
@CommandPermission("runic.op")
public class TravelCMD extends BaseCommand {

    // travel fast [player] [travelType] [location]

    public TravelCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("travelTypes", context -> {
            Set<String> travelTypes = new HashSet<>();
            for (TravelType travelType : TravelType.values()) {
                travelTypes.add(travelType.getIdentifier());
            }
            return travelTypes;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("travelLocations", context -> {
            Set<String> travelLocations = new HashSet<>();
            for (TravelLocation travelLocation : TravelLocation.values()) {
                travelLocations.add(travelLocation.getIdentifier());
            }
            return travelLocations;
        });
    }

    @Default
    @CatchUnknown
    @Subcommand("fast")
    @CommandCompletion("@players @travelTypes @travelLocations")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 3) {
            commandSender.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: travel fast [player] [travelType] [travelLocation]");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        TravelType travelType = TravelType.valueOf(args[1].toUpperCase());
        TravelLocation travelLocation = TravelLocation.getFromIdentifier(args[2]);
        fastTravelTask(player, travelType, travelLocation);
    }
}