package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicCombatExpEvent;
import com.runicrealms.plugin.events.RunicMobCombatExpEvent;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.party.PartyExpPayload;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("runicgive")
@CommandPermission("runic.op")
public class RunicGiveCMD extends BaseCommand {

    // runicgive exp [player] [amount] [x] [y] [z] [mobLevel]
    // runicgive exp [player] [amount] [uuidOfMob] [mobLevel]
    // runicgive exp [player] [amount] [quest]

    @Subcommand("exp")
    @CommandCompletion("@players")
    @Conditions("is-console-or-op")
    public void onCommandExp(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments.");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        int exp = Integer.parseInt(args[1]);

        // skip all other calculations for quest exp
        if (args.length == 3) {
            RunicCombatExpEvent e = new RunicCombatExpEvent(exp, false, player, RunicCombatExpEvent.RunicExpSource.QUEST, null);
            Bukkit.getPluginManager().callEvent(e);
            return;
        }

        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());

        // if the player doesn't have a party, or they're in by themselves, give them regular exp.
        if (party == null || party.getSize() < 2) {
            if (args.length != 6) {
                RunicCombatExpEvent e = new RunicCombatExpEvent(exp, true, player, RunicCombatExpEvent.RunicExpSource.DUNGEON, null);
                Bukkit.getPluginManager().callEvent(e);
            } else {
                int mobLv = Integer.parseInt(args[5]);
                Location loc = new Location(player.getWorld(), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                RunicCombatExpEvent e = new RunicMobCombatExpEvent(exp, true, player, mobLv, loc);
                Bukkit.getPluginManager().callEvent(e);
            }

            // otherwise, send party exp
        } else {
            int mobLevel = Integer.parseInt(args[5]);
            Location location = null;
            if (args.length == 6) {
                location = new Location(player.getWorld(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
            }
            PartyExpPayload payload = new PartyExpPayload(player, party, exp, mobLevel, location);
            payload.distributePartyExp();
        }
    }

}
