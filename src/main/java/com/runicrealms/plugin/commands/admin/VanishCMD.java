package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("vanish")
@Conditions("is-op")
@Private
public class VanishCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @CommandCompletion("@nothing")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.format("&r&cYou have entered improper arguments to execute this command!"));
        sender.sendMessage(ColorUtil.format("&r&c/vanish show {playerName}"));
        sender.sendMessage(ColorUtil.format("&r&c/vanish hide {playerName}"));
    }

    @Subcommand("hide")
    @CommandCompletion("@players @nothing")
    public void onHide(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.onHelp(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage(ColorUtil.format("&r&cYou have entered a name of a player that is not online!"));
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hidePlayer(RunicCore.getInstance(), target);
        }
    }

    @Subcommand("show")
    @CommandCompletion("@players @nothing")
    public void onShow(CommandSender sender, String[] args) {
        if (args.length != 1) {
            this.onHelp(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage(ColorUtil.format("&r&cYou have entered a name of a player that is not online!"));
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(RunicCore.getInstance(), target);
        }
    }
}
