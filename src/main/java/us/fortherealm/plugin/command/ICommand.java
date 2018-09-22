package us.fortherealm.plugin.command;

import org.bukkit.command.CommandSender;

public interface ICommand {
    void execute(CommandSender sender, String[] params);

    String getLabel();

    String getName();

    String getDescription();

    String getPermission();

    boolean canExecute(CommandSender sender);
}

