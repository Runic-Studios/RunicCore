package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.parties.Invite;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

public class Join implements SubCommand {

    private PartySC party;
    private Plugin plugin = RunicCore.getInstance();

    public Join(PartySC party) {
        this.party = party;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        Party party = RunicCore.getPartyManager().getPlayerParty(sender);

        // sender can't double-join parties
        if (party != null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are already in a party!");
            return;
        }

        Invite invite = RunicCore.getPartyManager().getActiveInvite(sender);

        // if the player has no active invite
        if (invite == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You don't have any invites!");
            return;
        }

        Party partyLead = RunicCore.getPartyManager().getPlayerParty(invite.getInviter());

        // if the party is disbanded before the player joins, remove the invite
        if (partyLead == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "This party no longer exists!");
            RunicCore.getPartyManager().removeInvite(invite);
            return;
        }

        // add the player by uuid to the party
        invite.getParty().addMember(sender.getUniqueId());

        // get this value updated for later
        party = RunicCore.getPartyManager().getPlayerParty(sender);

        // remove the invite from memory
        RunicCore.getPartyManager().removeInvite(invite);

        // inform the player which party they joined
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GREEN + "You joined "
                        + ChatColor.WHITE + invite.getInviter().getName()
                        + ChatColor.GREEN + "'s party!");

        // grab the player's stored name, convert it to a string
        String storedName = plugin.getConfig().get(sender.getUniqueId() + ".info.name").toString();

        // inform the party members of a new member, disclude the sender
        invite.getParty().sendOtherMembersMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + storedName
                        + ChatColor.GREEN + " joined the party!", sender.getUniqueId());

        // update the joiner's (sender) name for current members
        for (Player member : invite.getParty().getPlayerMembers()) {
            ScoreboardHandler.setPlayerTeamFor
                    (member, sender.getScoreboard().getTeam("party"),
                            Collections.singletonList(sender.getName()));
        }

        // update the party members' name colors for the joiner (sender)
        ScoreboardHandler.setPlayerTeamFor
                (sender, sender.getScoreboard().getTeam("party"),
                        invite.getParty().getPartyNames());

        // update the tablist
        for (Player member : party.getPlayerMembers()) {
            RunicCore.getTabListManager().setupTab(member);
        }
    }

	@Override
	public String permissionLabel() {
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
