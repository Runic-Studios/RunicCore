package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Party;

import java.util.List;

public class Leave implements SubCommand {
	
	private PartySC party;
	
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
		Party party = Main.getPartyManager().getPlayerParty(sender);
		
		if(party == null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You must be in a party to do this!");
			return;
		}
		
		if(!party.getLeader().equals(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You left your party!");
			party.removeMember(sender.getUniqueId());
			party.sendMessage("&3&lParty &7&l> &6" + sender.getName() + " &chas left the party!");
			return;
		}
		
		sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
				ChatColor.RED + "You left your party!");
		party.sendMemberMessage("&3&lParty &7&l> &cYour party has been disbanded. &7Reason: Leader Left");
		Main.getPartyManager().disbandParty(party);
	
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
