package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;

import java.util.List;
import java.util.UUID;

public class Leave implements SubCommand {

	private PartySC party;
    private Plugin plugin = RunicCore.getInstance();

	public Leave(PartySC party) {
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

		// if the sender is not in a party
		if(party == null) {
			sender.sendMessage
					(ChatColor.DARK_GREEN + "Party "
							+ ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are not in a party!");
			return;
		}

        // leaver is removed from the party
        party.removeMember(sender.getUniqueId());
        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);

        // reset the party member's name colors for the leaver
        //PartyDisconnect.updatePartyNames(party, sender, plugin);

        // if the new member count is less than 1, just disband the party
		if (party.getPartySize() < 1) {
            RunicCore.getPartyManager().disbandParty(party);
            RunicCore.getTabListManager().setupTab(sender);
        } else {

            // if the party leaver is not the leader
            if(!party.getLeader().equals(sender.getUniqueId())) {
                party.removeMember(sender.getUniqueId());
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + sender.getName() + " &chas left the party!");
            } else {

                // party leader is set to whoever is now in position [0]
                party.setLeader(party.getMemberUUID(0));

                // grab the new Player newLeader from their uuid in the party array
                Player newLeader = Bukkit.getPlayer(UUID.fromString(party.getLeader().toString()));

                // inform members of leader change
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + sender.getName()
                                + ChatColor.RED + " left the party. "
                                + ChatColor.WHITE + newLeader.getName()
                                + ChatColor.GREEN + " is now the party leader!");
            }
        }

        // send leaver message
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.RED + "You left your party!");

        // remove the leaver from the party array
        party.getPartyNames().remove(sender.getName());

        // update party nametag colors
        PartyDisconnect.updatePartyNames(party, sender);

        // update the tablist
        for (Player member : party.getPlayerMembers()) {
            RunicCore.getTabListManager().setupTab(member);
        }
        RunicCore.getTabListManager().setupTab(sender);
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
