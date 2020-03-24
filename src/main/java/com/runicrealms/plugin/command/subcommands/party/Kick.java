package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.parties.PartyDisconnect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Kick implements SubCommand {
	
	private PartySC party;
	private Plugin plugin = RunicCore.getInstance();
	
	public Kick(PartySC party) {
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
		
		if(party == null) {
			sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must be in a party to do this!");
			return;
		}

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must specify a player with "
                            + ChatColor.YELLOW + "/party kick <player>");
            return;
        }
		
		if(!party.getLeader().equals(sender.getUniqueId())) {
			sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must be the party leader to do this!");
			return;
		}
		
		Player target = Bukkit.getPlayer(args[1]);
		
		if(target == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "Player "
                            + ChatColor.WHITE + args[1]
                            + ChatColor.RED + " not found.");
			return;
		}
		
		if(target.getUniqueId().equals(sender.getUniqueId())) {
			party.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You can't kick yourself from the party!");
			return;
		}

		ArrayList<Player> members = party.getPlayerMembers();

		if(!members.contains(target)) {
			party.sendMessage
					(ChatColor.DARK_GREEN + "Party "
							+ ChatColor.GOLD + "» "
							+ ChatColor.RED + "This player is not in the party!");
			return;
		}

        String targetName = target.getName();
		String senderName = sender.getName();
		
		sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.RED + "You kicked "
                        + ChatColor.WHITE + targetName
                        + ChatColor.RED + " from your party!");

		// remove the player from the party and party array
        party.removeMember(target.getUniqueId());
        party.getPartyNames().remove(targetName);

        // reset the party member's name colors for the kicked player
        PartyDisconnect.updatePartyNames(party, target);

        target.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + senderName
                        + ChatColor.RED + " kicked you from the party!");

		party.sendMembersMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + senderName
                        + ChatColor.RED + " kicked "
                        + ChatColor.WHITE + targetName
                        + ChatColor.RED + " from the party!");

		// update the tablist
		for (Player member : members) {
			RunicCore.getTabListManager().setupTab(member);
		}
	}

    @Override
	public String permissionLabel() {
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
