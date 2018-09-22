package us.fortherealm.plugin.command;

import org.bukkit.Bukkit;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.commands.GetRune;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandListener implements Listener, CommandExecutor {
    private static Map<String, Command> commands;

    public CommandListener()
    {
        commands = new ConcurrentSkipListMap<String, Command>(String.CASE_INSENSITIVE_ORDER) {};
        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getInstance());
        this.addCommands();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
    {
        for(String cmd : commands.keySet()) {
            if (cmd.equalsIgnoreCase(label)) {
                Command pCommand = commands.get(cmd);
                if(pCommand.canExecute(sender))
                    pCommand.execute(sender, args);
                else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&4Error: You cannot use this command!"));
            }
        }

        return true;
    }


    public Map<String, Command> getCommands() {
        return commands;
    }

    private void addCommands() {
       this.getCommands().put("getrune", new GetRune());
    }

    private void reloadCommands() {}
}

