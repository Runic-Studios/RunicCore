package us.fortherealm.plugin.player.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.util.TabCompleteUtil;

import java.util.List;

public class SetLevelCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = FTRCore.getInstance();

    public SetLevelCMD(SetSC set) {
        this.set = set;
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

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set level [level] or /set level [player] [level]");
        } else if (args.length == 2) {
            sender.setLevel(Integer.parseInt(args[1]));
            sender.setExp(0);
        } else if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[1]);
            player.setLevel(Integer.parseInt(args[2]));
            sender.setExp(0);
        }
    }

    @Override
    public String permissionLabel() {
        return "set.level";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
