package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ColorUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@CommandAlias("experience|exp")
public class CheckExpCMD extends BaseCommand implements Listener {

    @Default
    @CatchUnknown
    @Conditions("is-player")
    public void onCommand(Player player) {
        int classLv = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassLevel();
        int classExp = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassExp();
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(classLv);
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(classLv + 1);
        double proportion = (double) (classExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel) * 100;
        NumberFormat toDecimal = new DecimalFormat("#0.00");
        String classProgressFormatted = toDecimal.format(proportion);

        int profLv = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfLevel();
        int profExp = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfExp();
        int profExpAtLevel = ProfExpUtil.calculateTotalExperience(profLv);
        int profTotalExpToLevel = ProfExpUtil.calculateTotalExperience(profLv + 1);
        double progress = (double) (profExp - profExpAtLevel) / (profTotalExpToLevel - profExpAtLevel);
        String profProgress = toDecimal.format(progress);

        player.sendMessage("");
        player.sendMessage(ColorUtil.format
                ("&a&lPlayer " + player.getName() +
                        "\n&7Class: &f" + classExp + " &7total exp, &f" + (classExp - totalExpAtLevel) + "&7/&f"
                        + (totalExpToLevel - totalExpAtLevel) + " &7exp to level &a(" + classProgressFormatted + "%)" +
                        "\n&7Profession: &f" + profExp + " &7total exp, &f" + (profExp - profExpAtLevel) + "&7/&f"
                        + (profTotalExpToLevel - profExpAtLevel) + " &7to level &a(" + profProgress + "%)"));
        player.sendMessage("");
    }
}