package com.runicrealms.plugin.fieldboss;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("fieldboss")
@CommandPermission("runic.op")
public class FieldBossCommand extends BaseCommand {
    
    @Default
    @CatchUnknown
    public void onCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Usage: /fieldboss activate|deactivate <identifier> [success]");
    }

    @Subcommand("activate")
    public void onActivateCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /fieldboss activate <identifier> [success]");
            return;
        }
        FieldBoss boss = RunicCore.getFieldBossAPI().getFieldBoss(args[0]);
        if (boss == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid field boss identifier!");
            return;
        }
        boolean success = boss.attemptActivate(sender);
        if (success) sender.sendMessage(ChatColor.GREEN + "Activated field boss");
    }

    @Subcommand("deactivate")
    public void onDeactivateCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /fieldboss deactivate <identifier> [success]");
            return;
        }
        boolean success = false;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("true")) {
                success = true;
            } else if (!args[1].equalsIgnoreCase("false")) {
                sender.sendMessage(ChatColor.RED + "Unknown success value " + args[1]);
                return;
            }
        }
        FieldBoss boss = RunicCore.getFieldBossAPI().getFieldBoss(args[0]);
        if (boss == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not a valid field boss identifier!");
            return;
        }
        FieldBoss.ActiveState state = boss.getActiveState();
        if (state == null) {
            sender.sendMessage(ChatColor.RED + "Cannot deactivate field boss because it is not active!");
            return;
        }
        state.deactivate(success);
        sender.sendMessage(ChatColor.GREEN + "Deactivated field boss with identifier " + args[0]);
    }

}
