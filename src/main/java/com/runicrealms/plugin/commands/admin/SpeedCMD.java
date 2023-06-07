package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@CommandAlias("speed")
@CommandPermission("runic.op")
public class SpeedCMD extends BaseCommand {

    // speed [speed]

    @Default
    @CatchUnknown
    @Syntax("<player> <class>")
    @Conditions("is-op")
    public void onCommandSpeed(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: speed {speed}");
            return;
        }
        try {
            float speed = Float.parseFloat(args[0]);
            if (speed < 1f)
                speed = 1f;
            if (speed > 10f)
                speed = 10f;
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            player.setFlySpeed(speed / 5f); // default is 0.2, so speed 1 / 5 = 0.2
            player.sendMessage(ChatColor.GREEN + "You have set your fly speed to " + (int) speed + "!");
            player.sendMessage(ChatColor.YELLOW + "Now stop asking Sky please.");
        } catch (Exception ex) {
            player.sendMessage(ChatColor.RED + "You have entered an incorrect argument.");
        }
    }

}
