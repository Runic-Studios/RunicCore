package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.classes.SelectClass;

import java.util.List;

public class Help implements SubCommand {
	
	private PartySC party;

	private SelectClass partyHelpGUI;
	
	public Help(PartySC party) {
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
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GRAY + "Available commands: "
                        + ChatColor.YELLOW + "create, disband, help, invite, join, kick, leave");

        SelectClass.CLASS_SELECTION.open(sender);
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
