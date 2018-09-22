package us.fortherealm.plugin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class Command implements ICommand {
    private String label, description, permission;

    public Command(String label, String description, String permission)
    {
        this.label = label;
        this.description = description;
        this.permission = permission;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(sender == null) return;

        if(sender instanceof ConsoleCommandSender)
        {
            this.onConsoleCommand(sender, args);
        }
        else
        {
            Player player = (Player) sender;

            if(player.isOp() || player.hasPermission("ftr.staffmode"))
            {
                this.onOPCommand(player, args);
            }
            else
            {
                this.onUserCommand(player, args);
            }
        }
    }

    public abstract void onConsoleCommand(CommandSender sender, String[] params);
    public abstract void onOPCommand(Player sender,String[] params);
    public abstract void onUserCommand(Player sender, String[] params);

    @Override
    public boolean canExecute(CommandSender sender) {return sender.hasPermission(this.getPermission()); }

    @Override
    public String getName() {return this.getLabel();}

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getPermission() {
        return this.permission;
    }

    protected String color(String message){return ChatColor.translateAlternateColorCodes('&', message);}
}

