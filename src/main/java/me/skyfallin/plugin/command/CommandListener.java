package me.skyfallin.plugin.command;

import me.skyfallin.plugin.Main;
import me.skyfallin.plugin.command.commands.GetRune;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandListener implements Listener, CommandExecutor {
    private static Main plugin;
    private static final Map<String, Command> commands = new ConcurrentSkipListMap<String, Command>(String.CASE_INSENSITIVE_ORDER) {};

    public CommandListener(Plugin plugin)
    {
        plugin = plugin;
        plugin.getLogger().info("Listener initialized.");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    public static void addCommand(String label, Command command) {
        plugin.getLogger().info("Registered command: " + label);
        commands.put(label, command);
    }

    public void addCommands() {
        addCommand("getrune", new GetRune());
    }

    private void reloadCommands() {}
}

