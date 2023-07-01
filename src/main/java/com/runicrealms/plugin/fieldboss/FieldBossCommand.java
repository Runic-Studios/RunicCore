package com.runicrealms.plugin.fieldboss;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("fieldboss")
@CommandPermission("runic.op")
public class FieldBossCommand extends BaseCommand {

    private static boolean isDouble(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Default
    @CatchUnknown
    public void onCommand(Player executor) {
        executor.sendMessage(ChatColor.RED + "Usage: /fieldboss activate|deactivate");
    }

    @Subcommand("activate")
    public void onActivateCommand(Player executor, String[] args) {
        if (args.length != 1) {
            executor.sendMessage(ChatColor.RED + "Usage: /fieldboss activate <identifier>");
            return;
        }
        RunicCore.getFieldBossAPI().getFieldBoss(args[0]).attemptActivate(executor);
//        FieldBoss boss = new FieldBoss(identifier, mmID, executor.getLocation(), radius);
//        bosses.put(boss.getIdentifier(), boss);
//        boss.activate();
        executor.sendMessage(ChatColor.GREEN + "Activated field boss");
    }

//    @Subcommand("deactivate")
//    public void onDeactivateCommand(Player executor, String[] args) {
//        if (args.length != 1) {
//            executor.sendMessage(ChatColor.RED + "Usage: /fieldboss deactivate <identifier>");
//            return;
//        }
//        if (!bosses.containsKey(args[0])) {
//            executor.sendMessage(ChatColor.RED + "Fieldboss with identifier " + args[0] + " not found");
//            return;
//        }
//        bosses.get(args[0]).deactivate();
//        bosses.remove(args[0]);
//        executor.sendMessage(ChatColor.GREEN + "Deactivated fieldboss with identifier " + args[0]);
//    }

}
