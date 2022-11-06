package com.runicrealms.plugin.party;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.event.LeaveReason;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@CommandAlias("party")
public class PartyCommand extends BaseCommand {

    private static final String PREFIX = "&2[Party] &6»";

    public PartyCommand() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-invite", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) return new ArrayList<>();
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer())
                return new ArrayList<>();
            Set<String> players = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
                    players.add(player.getName());
                }
            }
            return players;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-join", context -> {
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) != null) return new ArrayList<>();
            Set<String> invites = new HashSet<>();
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
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()) == null) return new ArrayList<>();
            if (RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getLeader() != context.getPlayer())
                return new ArrayList<>();
            Set<String> members = new HashSet<>();
            RunicCore.getPartyManager().getPlayerParty(context.getPlayer()).getMembers().forEach(member -> members.add(member.getName()));
            return members;
        });
    }

    @Subcommand("create|c")
    @Conditions("is-player")
    public void onCommandCreate(Player player) {
        if (!RunicCore.getPartyManager().canJoinParty(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou are already in a party/group!"));
            return;
        }
        Party party = new Party(player);
        RunicCore.getPartyManager().getParties().add(party);
        RunicCore.getPartyManager().updatePlayerParty(player, party);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aYou created a party! Use &2/party invite &ato invite players"));
        RunicCore.getTabAPI().setupTab(player);
    }

    @Subcommand("disband|d|delete")
    @Conditions("is-player")
    public void onCommandDisband(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be in a party to use this command!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(player).getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be party leader to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
        for (Player member : party.getMembersWithLeader()) {
            RunicCore.getPartyManager().updatePlayerParty(member, null);
            RunicCore.getTabAPI().setupTab(member);
        }
        PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, party.getLeader(), LeaveReason.DISBAND);
        Bukkit.getPluginManager().callEvent(partyLeaveEvent);
        RunicCore.getPartyManager().getParties().remove(party);
    }

    @Default
    @CatchUnknown
    @Subcommand("help|h")
    public void onCommandHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aAvailable commands: &ecreate, disband, help, invite, join, kick, leave, list"));
    }

    @Subcommand("invite|add|i|a")
    @Syntax("<player>")
    @CommandCompletion("@party-invite")
    @Conditions("is-player")
    public void onCommandInvite(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cPlease specify a player to invite!"));
            return;
        }
        Player invited = Bukkit.getPlayerExact(args[0]);
        if (invited == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not online!"));
            return;
        }
        if (!RunicCore.getPartyManager().canJoinParty(invited)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is already in a party/group!"));
            return;
        }
        if (RunicCore.getPartyManager().memberHasInvite(invited)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has already been invited to your/a different party!"));
            return;
        }
        if (invited.equals(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou cannot invite yourself!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            onCommandCreate(player);
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party.getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be party leader to use this command!"));
            return;
        }
        invited.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aYou have been invited to " + player.getName() + "'s party, type &2/party join " + player.getName() + " &ato join."));
        party.sendMessageInChannel(player.getName() + " has invited " + invited.getName() + " to the party");
        party.addInvite(invited);
    }

    @Subcommand("join|j")
    @Syntax("<party-owner>")
    @CommandCompletion("@party-join")
    @Conditions("is-player")
    public void onCommandJoin(Player player, String[] args) {
        if (!RunicCore.getPartyManager().canJoinParty(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou cannot use this command while in a party/group!"));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cPlease specify the name of the person that invited you to their party"));
            return;
        }
        Player inviter = Bukkit.getPlayerExact(args[0]);
        if (inviter == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not online!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(inviter);
        if (party == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has not invited you to their party!"));
            return;
        }
        Party.Invite invite = party.getInvite(player);
        if (invite == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has not invited you to their party!"));
            return;
        }
        party.acceptMemberInvite(player);
        party.sendMessageInChannel(player.getName() + " has joined the party");
        RunicCore.getPartyManager().updatePlayerParty(player, party);
    }

    @Subcommand("kick|k")
    @Syntax("<player>")
    @CommandCompletion("@party-kick")
    @Conditions("is-player")
    public void onCommandKick(Player player, String[] args) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party.getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be party leader to use this command!"));
            return;
        }
        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cPlease specify which player to kick"));
            return;
        }
        Player kicked = Bukkit.getPlayerExact(args[0]);
        if (kicked == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not online!"));
            return;
        }
        if (kicked == player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou cannot kick yourself!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(kicked) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not in your party!"));
            return;
        }
        if (RunicCore.getPartyManager().getPlayerParty(kicked) != party) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not in your party!"));
            return;
        }
        //party.getMembers().remove(kicked);
        party.kickMember(kicked, LeaveReason.KICK);
        RunicCore.getPartyManager().updatePlayerParty(kicked, null);
        RunicCore.getTabAPI().setupTab(kicked);
        for (Player member : party.getMembersWithLeader()) {
            RunicCore.getTabAPI().setupTab(member);
        }
        party.sendMessageInChannel(kicked.getName() + " has been removed from this party &7Reason: kicked");
        kicked.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aYou have been kicked from the party!"));
    }

    @Subcommand("leave|quit|q")
    @Conditions("is-player")
    public void onCommandLeave(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party.getLeader() == player) {
            party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
            PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, party.getLeader(), LeaveReason.DISBAND);
            Bukkit.getPluginManager().callEvent(partyLeaveEvent);
            for (Player member : party.getMembersWithLeader()) {
                RunicCore.getPartyManager().updatePlayerParty(member, null);
                RunicCore.getTabAPI().setupTab(member);
            }
        } else {
            party.sendMessageInChannel(player.getName() + " has been removed this party &7Reason: left");
            PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, player, LeaveReason.LEAVE);
            Bukkit.getPluginManager().callEvent(partyLeaveEvent);
            party.getMembers().remove(player);
            RunicCore.getPartyManager().updatePlayerParty(player, null);
            RunicCore.getTabAPI().setupTab(player);
            for (Player member : party.getMembersWithLeader()) {
                RunicCore.getTabAPI().setupTab(member);
            }
        }
    }

    @Subcommand("list|players|members|l")
    @Conditions("is-player")
    public void onCommandList(Player player) {
        if (RunicCore.getPartyManager().getPlayerParty(player) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        StringBuilder builder = new StringBuilder();
        builder.append(PREFIX + " &aMembers: &e");
        int i = 0;
        int last = party.getMembers().size();
        for (Player member : party.getMembersWithLeader()) {
            builder.append(member.getName());
            if (i != last) {
                builder.append(", ");
            }
            i++;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
    }

    @Subcommand("tp|teleport")
    @Syntax("<player>")
    @Conditions("is-op")
    public void onCommandTeleport(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPlease provide the player to teleport party members to!"));
            return;
        }
        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat player is not online!"));
            return;
        }
        Party party = RunicCore.getPartyManager().getPlayerParty(player);
        if (party == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat player is not in a party!"));
            return;
        }
        for (Player target : party.getMembersWithLeader()) {
            if (target == player) continue;
            if (target.getWorld() != player.getWorld()) continue;
            target.teleport(player.getLocation());
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou were teleported with your party!"));
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTeleported party members to your location!"));
    }

}
