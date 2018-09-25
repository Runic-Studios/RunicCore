package us.fortherealm.plugin.command.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public interface SubCommand extends TabCompleter {
	
	void onConsoleCommand(CommandSender sender, String[] args);
	void onOPCommand(Player sender, String[] args);
	void onUserCommand(Player sender, String[] args);
	
	String permissionLabel();
}
