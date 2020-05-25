package com.runicrealms.plugin.parties.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.parties.PartyChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@CommandAlias("party")
public class PartyCommand extends BaseCommand {

    public PartyCommand() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-invite", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) {
                return new ArrayList<String>();
            }
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer()) {
                return new ArrayList<String>();
            }
            Set<String> players = new HashSet<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
                    players.add(player.getName());
                }
            }
            return players;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-join", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) {
                return new ArrayList<String>();
            }
            Set<String> invites = new HashSet<String>();
            for (Party party : RunicCore.getPartyManager().getParties()) {
                for (Party.Invite invite : party.getInvites()) {
                    if (invite.getPlayer() == context.getPlayer()) {
                        invites.add(party.getLeader().getName());
                    }
                }
            }
            return invites;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-kick", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) {
                return new ArrayList<String>();
            }
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer()) {
                return new ArrayList<String>();
            }
            Set<String> members = new HashSet<String>();
            RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getMembers().forEach(member -> members.add(member.getName()));
            return members;
        });
        RunicCore.getCommandManager().getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) {
                throw new ConditionFailedException("This command cannot be run from console!");
            }
        }));
    }

    @Default
    @CatchUnknown
    @Subcommand("help|h")
    @Conditions("is-player")
    public void onCommandHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &7Available commands: &ecreate, disband, help, invite, join, kick, leave"));
    }

    @Subcommand("create|c")
    @Conditions("is-player")
    public void onCommandCreate(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou are already in a party!"));
            return;
        }
        Party party = new Party(player);
        RunicCore.getPartyManager().getParties().add(party);
        RunicCore.getPartyManager().updatePlayerParty(player, party);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        player.sendMessage("&2Party &6» &aYou created a party! Use &2/party invite &ato invite players.");
        RunicCore.getTabListManager().setupTab(player);
    }

    @Subcommand("disband|d|delete")
    @Conditions("is-player")
    public void onCommandDisband(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be in a party to use this command!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(player).getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be party leader to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        for (Player member : party.getMembers()) {
            RunicCore.getPartyManager().updatePlayerParty(member, null);
        }
        RunicCore.getPartyManager().updatePlayerParty(party.getLeader(), null);
        PartyChannel.sendInPartyChat(party, RunicCore.getPartyChatChannel().getPrefix() + "This party has been disbanded");
        RunicCore.getPartyManager().getParties().remove(party);
    }

    @Subcommand("invite|add|i|a")
    @Syntax("<player>")
    @CommandCompletion("@party-invite")
    @Conditions("is-player")
    public void onCommandInvite(Player player, String[] args) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be in a party to use this command!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(player).getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be party leader to use this command!"));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cPlease specify a player to invite!"));
            return;
        }
        Player invited = Bukkit.getPlayerExact(args[0]);
        if (invited == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not online!"));
            return;
        }
    }

    @Subcommand("join|j")
    @Syntax("<party-owner>")
    @CommandCompletion("@party-join")
    @Conditions("is-player")
    public void onCommandJoin(Player player, String[] args) {

    }

    @Subcommand("kick|k")
    @Syntax("<player>")
    @CommandCompletion("@party-kick")
    @Conditions("is-player")
    public void onCommandKick(Player player, String[] args) {

    }

    @Subcommand("leave|l|quit|q")
    @Conditions("is-player")
    public void onCommandLeave(Player player) {

    }

}
