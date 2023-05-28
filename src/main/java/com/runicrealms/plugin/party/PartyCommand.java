package com.runicrealms.plugin.party;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.party.event.LeaveReason;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@CommandAlias("party")
public class PartyCommand extends BaseCommand {

    private static final String PREFIX = "&2[Party] &6»";

    public PartyCommand() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-invite", context -> {
            if (RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()) == null)
                return new ArrayList<>();
            if (RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()).getLeader() != context.getPlayer())
                return new ArrayList<>();
            Set<String> players = new HashSet<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
                    players.add(player.getName());
                }
            }
            return players;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-join", context -> {
            if (RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()) != null)
                return new ArrayList<>();
            Set<String> invites = new HashSet<>();
            for (Party party : RunicCore.getPartyAPI().getParties()) {
                for (Party.Invite invite : party.getInvites()) {
                    if (invite.getPlayer() == context.getPlayer()) {
                        invites.add(party.getLeader().getName());
                    }
                }
            }
            return invites;
        });
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("party-kick", context -> {
            if (RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()) == null)
                return new ArrayList<>();
            if (RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()).getLeader() != context.getPlayer())
                return new ArrayList<>();
            Set<String> members = new HashSet<>();
            RunicCore.getPartyAPI().getParty(context.getPlayer().getUniqueId()).getMembers().forEach(member -> members.add(member.getName()));
            return members;
        });
    }

    @Subcommand("create|c")
    @Conditions("is-player")
    public void onCommandCreate(Player player) {
        if (!RunicCore.getPartyAPI().canJoinParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou are already in a party/group!"));
            return;
        }
        Party party = new Party(player);
        RunicCore.getPartyAPI().getParties().add(party);
        RunicCore.getPartyAPI().updatePlayerParty(player.getUniqueId(), party);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aYou created a party! Use &2/party invite &ato invite players"));
        RunicCore.getTabAPI().setupTab(player);
    }

    @Subcommand("disband|d|delete")
    @Conditions("is-player")
    public void onCommandDisband(Player player) {
        if (!RunicCore.getPartyAPI().hasParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be in a party to use this command!"));
            return;
        }
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()).getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be party leader to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
        for (Player member : party.getMembersWithLeader()) {
            RunicCore.getPartyAPI().updatePlayerParty(member.getUniqueId(), null);
            RunicCore.getTabAPI().setupTab(member);
        }
        PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, party.getLeader(), LeaveReason.DISBAND);
        Bukkit.getPluginManager().callEvent(partyLeaveEvent);
        RunicCore.getPartyAPI().getParties().remove(party);
    }

    @Default
    @CatchUnknown
    @Subcommand("help|h")
    public void onCommandHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &aAvailable commands: &ecreate, disband, help, invite, join, kick, leave, list"));
    }

    @Subcommand("invite|add|i|a")
    @Syntax("<player>")
    @CommandCompletion("@online")
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
        if (!RunicCore.getPartyAPI().canJoinParty(invited.getUniqueId())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is already in a party/group!"));
            return;
        }
        if (RunicCore.getPartyAPI().memberHasInvite(invited)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has already been invited to your/a different party!"));
            return;
        }
        if (invited.equals(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou cannot invite yourself!"));
            return;
        }
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
            onCommandCreate(player);
        }
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        if (party.getLeader() != player) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou must be party leader to use this command!"));
            return;
        }
        String inviteMessage = ChatColor.translateAlternateColorCodes('&',
                PREFIX + " &aYou have been invited to " + player.getName() + "'s party!");
        String clickableMessageSpaces = ChatUtils.centeredMessage(player, ChatColor.GOLD + "[Click Here]" + ChatColor.GREEN + " to join");
        TextComponent textComponent = new TextComponent(clickableMessageSpaces + ChatColor.GOLD + "[Click Here]");
        textComponent.setClickEvent(new ClickEvent
                (
                        ClickEvent.Action.RUN_COMMAND,
                        "/party join " + player.getName()
                ));
        textComponent.setHoverEvent(new HoverEvent
                (
                        HoverEvent.Action.SHOW_TEXT,
                        new Text(ChatColor.GREEN + "Join " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + "'s party")
                ));
        invited.sendMessage("");
        invited.sendMessage(inviteMessage);
        invited.spigot().sendMessage(textComponent, new TextComponent(ChatColor.GREEN + " to " +
                "join"));
        invited.sendMessage("");
        party.sendMessageInChannel(player.getName() + " has invited " + invited.getName() + " to the party");
        party.addInvite(invited);
    }

    @Subcommand("join|j")
    @Syntax("<party-owner>")
    @CommandCompletion("@party-join")
    @Conditions("is-player")
    public void onCommandJoin(Player player, String[] args) {
        if (!RunicCore.getPartyAPI().canJoinParty(player.getUniqueId())) {
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
        Party party = RunicCore.getPartyAPI().getParty(inviter.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has not invited you to their party!"));
            return;
        }
        Party.Invite invite = party.getInvite(player);
        if (invite == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player has not invited you to their party!"));
            return;
        }
        boolean joinedParty = party.acceptMemberInvite(player);
        if (joinedParty) {
            party.sendMessageInChannel(player.getName() + " has joined the party");
            RunicCore.getPartyAPI().updatePlayerParty(player.getUniqueId(), party);
        }
    }

    @Subcommand("kick|k")
    @Syntax("<player>")
    @CommandCompletion("@party-kick")
    @Conditions("is-player")
    public void onCommandKick(Player player, String[] args) {
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
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
        if (RunicCore.getPartyAPI().getParty(kicked.getUniqueId()) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not in your party!"));
            return;
        }
        if (RunicCore.getPartyAPI().getParty(kicked.getUniqueId()) != party) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cThat player is not in your party!"));
            return;
        }
        //party.getMembers().remove(kicked);
        party.kickMember(kicked, LeaveReason.KICK);
        RunicCore.getPartyAPI().updatePlayerParty(kicked.getUniqueId(), null);
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
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        if (party.getLeader() == player) {
            party.sendMessageInChannel("This party has been disbanded &7Reason: leader disbanded");
            PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, party.getLeader(), LeaveReason.DISBAND);
            Bukkit.getPluginManager().callEvent(partyLeaveEvent);
            for (Player member : party.getMembersWithLeader()) {
                RunicCore.getPartyAPI().updatePlayerParty(member.getUniqueId(), null);
                RunicCore.getTabAPI().setupTab(member);
            }
        } else {
            party.sendMessageInChannel(player.getName() + " has been removed this party &7Reason: left");
            PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, player, LeaveReason.LEAVE);
            Bukkit.getPluginManager().callEvent(partyLeaveEvent);
            party.getMembers().remove(player);
            RunicCore.getPartyAPI().updatePlayerParty(player.getUniqueId(), null);
            RunicCore.getTabAPI().setupTab(player);
            for (Player member : party.getMembersWithLeader()) {
                RunicCore.getTabAPI().setupTab(member);
            }
        }
    }

    @Subcommand("list|players|members|l")
    @Conditions("is-player")
    public void onCommandList(Player player) {
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &cYou need to be in a party to use this command!"));
            return;
        }
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
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
        Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());
        if (party == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat player is not in a party!"));
            return;
        }
        for (Player target : party.getMembersWithLeader()) {
            if (target.equals(player)) continue;
            if (target.getWorld() != player.getWorld()) continue;
            double squaredDistance = player.getLocation().distanceSquared(target.getLocation());
            // Compare the squared distance to the squared threshold (128 * 128 = 16384)
            if (squaredDistance > 16384) continue;
            target.teleport(player);
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou were teleported with your party!"));
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTeleported nearby party members to your location!"));
    }

}
