package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Party;

import java.util.List;

public class Kick implements SubCommand {
	
	private PartySC party;
	
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
		Party party = Main.getPartyManager().getPlayerParty(sender);
		
		if(party == null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You must be in a party to do this!");
			return;
		}
		
		if(party.getLeader().equals(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You must be the party leader to do this!");
			return;
		}
		
		if(args[1] == null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "Not Enough Arguments");
			return;
		}
		
		Player target = Bukkit.getPlayer(args[1]);
		
		if(target == null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "Player " + ChatColor.GOLD + args[1] + ChatColor.RED + " not found!");
			return;
		}
		
		if(target.getUniqueId().equals(sender.getUniqueId())) {
			party.sendMessage("&3&lParty &7&l> &cYou can't kick yourself from the party!");
			return;
		}
		
		sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
				ChatColor.RED + "You kicked " + ChatColor.GOLD + target.getName() + ChatColor.RED + " from your party!");
		party.removeMember(target.getUniqueId());
		party.sendMemberMessage("&3&lParty &7&l> &6" + sender.getName() + " &chas kicked &e" + args[1] + " &c from the party!");
		
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
