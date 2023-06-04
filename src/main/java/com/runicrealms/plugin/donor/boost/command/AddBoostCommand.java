package com.runicrealms.plugin.donor.boost.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.donor.boost.api.StoreBoost;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("addboost")
@Conditions("is-op")
public class AddBoostCommand extends BaseCommand {

    @CatchUnknown
    @Default
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ColorUtil.format("&cInvalid usage, try: /addboost <target-uuid> <boosttype>"));
            return;
        }
        UUID target;
        try {
            target = UUID.fromString(args[0]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ColorUtil.format("&cInvalid target UUID in arg position 1"));
            return;
        }
        StoreBoost boost = StoreBoost.getFromIdentifier(args[1]);
        if (boost == null) {
            sender.sendMessage(ColorUtil.format("&cInvalid boost type: " + args[0]));
            return;
        }
        RunicCore.getBoostAPI().addStoreBoost(target, boost);

        sender.sendMessage(ChatColor.GREEN + "Added " + boost.getName() + " Boost for " + target);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(target)) {
                online.sendMessage(ColorUtil.format("&5[Runic Realms] &dYou have purchased a &r&f&l"
                        + boost.getName()
                        + " Experience Boost&r&d. Thank you for supporting the project!"));
                return;
            }
        }
    }
}
