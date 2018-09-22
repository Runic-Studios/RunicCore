package us.fortherealm.plugin.command;

import us.fortherealm.plugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public abstract class Command implements ICommand {
    private String label, description, permission;
    private String[] usage;
    private int argsCount;

    public Command(String label, int argsCount, String[] usage, String description, String permission)
    {
        this.label = label;
        this.description = description;
        this.permission = permission;
        this.usage = usage;
        this.argsCount = argsCount;

        CommandListener.addCommand(label, this);
        Main.getPlugin(Main.class).getLogger().info("Command initialized: " + label);
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if(sender == null) return;

        if(this.getArgsCount() != args.length)
        {
            sender.sendMessage("Usage: " + getUsage());
        }

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
    public int getArgsCount() {
        return this.argsCount;
    }

    @Override
    public String getUsage() {
        return String.join(" ", this.usage);
    }

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

