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
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) return new ArrayList<String>();
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer()) return new ArrayList<String>();
            Set<String> players = new HashSet<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
                    players.add(player.getName());
                }
            }
            return players;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-join", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) return new ArrayList<String>();
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
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) return new ArrayList<String>();
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer()) return new ArrayList<String>();
            Set<String> members = new HashSet<String>();
            RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getMembers().forEach(member -> members.add(member.getName()));
            return members;
        });
        RunicCore.getCommandManager().getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) throw new ConditionFailedException("This command cannot be run from console!");
        });
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
        if (RunicCore.getPartyManager().getPlayerParty(player) != null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou are already in a party!")); return; }
        Party party = new Party(player);
        RunicCore.getPartyManager().getParties().add(party);
        RunicCore.getPartyManager().updatePlayerParty(player, party);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &aYou created a party! Use &2/party invite &ato invite players"));
        RunicCore.getTabListManager().setupTab(player);
    }

    @Subcommand("disband|d|delete")
    @Conditions("is-player")
    public void onCommandDisband(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be in a party to use this command!")); return; }
        if (RunicCore.getPartyManager().getPlayerParty(player).getLeader() != player) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be party leader to use this command!")); return; }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
        party.disband();
        for (Player member : party.getMembers()) {
            RunicCore.getPartyManager().updatePlayerParty(member, null);
        }
        RunicCore.getPartyManager().updatePlayerParty(party.getLeader(), null);
        RunicCore.getPartyManager().getParties().remove(party);
    }

    @Subcommand("invite|add|i|a")
    @Syntax("<player>")
    @CommandCompletion("@party-invite")
    @Conditions("is-player")
    public void onCommandInvite(Player player, String[] args) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be in a party to use this command!")); return; }
        if (RunicCore.getPartyManager().getPlayerParty(player).getLeader() != player) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou must be party leader to use this command!")); return; }
        if (args.length < 1) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cPlease specify a player to invite!")); return; }
        Player invited = Bukkit.getPlayerExact(args[0]);
        if (invited == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not online!")); return; }
        if (RunicCore.getPartyManager().getPlayerParty(invited) != null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is already in a party!")); return; }
        if (RunicCore.getPartyManager().memberHasInvite(invited)) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player has been invited to a different party!")); return; }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        invited.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &aYou have been invited to " + player.getName() + "'s party, type &2/party join " + player.getName() + " &ato join."));
        party.sendMessageInChannel(player.getName() + " has invited " + invited.getName() + " to the party");
        party.addInvite(player);
    }

    @Subcommand("join|j")
    @Syntax("<party-owner>")
    @CommandCompletion("@party-join")
    @Conditions("is-player")
    public void onCommandJoin(Player player, String[] args) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou cannot use this command while in a party!")); return; }
        if (args.length < 1) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cPlease specify the name of the person that invited you to their party")); return; }
        Player inviter = Bukkit.getPlayerExact(args[0]);
        if (inviter == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not online!")); return; }
        Party party = RunicCore.getPartyManager().getPlayerParty(inviter);
        if (party == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player has not invited you to their party!")); return; }
        Party.Invite invite = party.getInvite(player);
        if (invite == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player has not invited you to their party!")); return; }
        party.acceptMemberInvite(player);
        party.sendMessageInChannel(player.getName() + " has joined the party");
        RunicCore.getPartyManager().updatePlayerParty(player, party);
    }

    @Subcommand("kick|k")
    @Syntax("<player>")
    @CommandCompletion("@party-kick")
    @Conditions("is-player")
    public void onCommandKick(Player player, String[] args) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou need to be in a party to use this command!")); return; }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party.getLeader() != player) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou need to be party leader to use this command!")); return; }
        if (args.length < 1) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cPlease specify which player to kick")); return; }
        Player kicked = Bukkit.getPlayerExact(args[0]);
        if (kicked == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not online!")); return; }
        if (RunicCore.getPartyManager().getPlayerParty(kicked) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not in your party!")); return; }
        if (RunicCore.getPartyManager().getPlayerParty(kicked) != party) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cThat player is not in your party!")); return; }
        party.getMembers().remove(kicked);
        RunicCore.getPartyManager().updatePlayerParty(kicked, null);
        party.sendMessageInChannel(kicked + " has been removed from this party &7Reason: kicked");
    }

    @Subcommand("leave|l|quit|q")
    @Conditions("is-player")
    public void onCommandLeave(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) { player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2Party &6» &cYou need to be in a party to use this command!")); return; }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party.getLeader() == player) {
            party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
            for (Player member : party.getMembers()) {
                RunicCore.getPartyManager().updatePlayerParty(member, null);
            }
            RunicCore.getPartyManager().updatePlayerParty(party.getLeader(), null);
            party.disband();
            RunicCore.getPartyManager().getParties().remove(party);
        } else {
            party.sendMessageInChannel(player + " has been removed this party &7Reason: left");
            party.getMembers().remove(player);
            RunicCore.getPartyManager().updatePlayerParty(player, null);
        }
    }

}
