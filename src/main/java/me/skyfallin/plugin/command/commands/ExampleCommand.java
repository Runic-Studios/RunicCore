package me.skyfallin.plugin.command.commands;

import me.skyfallin.plugin.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExampleCommand extends Command {

    public ExampleCommand() {
        super("example", 2, new String[]{"/example", "args1", "args2"}, "An example command!", "example.command.permission");
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] params) {
        sender.sendMessage("Example Command - Ran from console!");
    }

    @Override
    public void onOPCommand(Player sender, String[] params) {
        sender.sendMessage("Example Command - Ran from a user with Admin/OP!");
    }

    @Override
    public void onUserCommand(Player sender, String[] params) {
        sender.sendMessage(color("&cExample Command - &6Ran by a &bplayer&6!"));
    }
}
