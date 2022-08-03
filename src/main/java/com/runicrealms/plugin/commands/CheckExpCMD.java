package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.redis.RedisField;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;

@CommandAlias("experience|exp")
public class CheckExpCMD extends BaseCommand implements Listener {

    @Default
    @CatchUnknown
    @Conditions("is-player")
    public void onCommand(Player player) {
        UUID uuid = player.getUniqueId();
        int classLv = Integer.parseInt(RunicCoreAPI.getRedisValue(uuid, RedisField.CLASS_LEVEL.getField()));
        int classExp = Integer.parseInt(RunicCoreAPI.getRedisValue(uuid, RedisField.CLASS_EXP.getField()));
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(classLv);
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(classLv + 1);
        double proportion = (double) (classExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel) * 100;
        NumberFormat toDecimal = new DecimalFormat("#0.00");
        String classProgressFormatted = toDecimal.format(proportion);

        int profLv = Integer.parseInt(RunicCoreAPI.getRedisValue(uuid, RedisField.PROF_LEVEL.getField()));
        int profExp = Integer.parseInt(RunicCoreAPI.getRedisValue(uuid, RedisField.PROF_EXP.getField()));
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