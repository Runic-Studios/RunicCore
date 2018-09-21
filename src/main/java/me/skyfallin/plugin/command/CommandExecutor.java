package me.skyfallin.plugin.command;

import me.skyfallin.plugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class CommandExecutor implements Listener {
    private Main plugin;
    private static final Map<String, Command> commands = new ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER) {};

    public CommandExecutor(Plugin plugin)
    {
        this.plugin = (Main) plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
    {
        for(String cmd : commands.keySet()) {
            if (cmd.equalsIgnoreCase(label)) {
                Command pCommand = commands.get(cmd);
                if(pCommand.canExecute(sender))
                    pCommand.execute(sender, args);
                else
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cError: &4You cannot use this command!"));
            }
        }

        return true;
    }


    public Map<String, Command> getCommands() {
        return commands;
    }

    public static void addCommand(String label, Command command) {
        commands.put(label, command);
    }

    private void reloadCommands() {}
}

