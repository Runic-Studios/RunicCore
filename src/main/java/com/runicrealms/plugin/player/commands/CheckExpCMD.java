package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.command.supercommands.SuperCommand;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CheckExpCMD extends SuperCommand {

    public CheckExpCMD() {
        super("runic.checkexp");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {

            Player pl = (Player) sender;

            int classLv = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel();
            int classExp = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassExp();
            int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(classLv);
            int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(classLv+1);
            double proportion = (double) (classExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel) * 100;
            NumberFormat toDecimal = new DecimalFormat("#0.00");
            String classProgressFormatted = toDecimal.format(proportion);

            int profLv = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getProfLevel();
            int profExp = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getProfExp();
            int profExpAtLevel = ProfExpUtil.calculateTotalExperience(profLv);
            int profTotalExpToLevel = ProfExpUtil.calculateTotalExperience(profLv+1);
            double progress = (double) (profExp-profExpAtLevel) / (profTotalExpToLevel-profExpAtLevel);
            String profProgress = toDecimal.format(progress);

            sender.sendMessage("");
            sender.sendMessage(ColorUtil.format
                    ("&a&lPlayer " + sender.getName() +
                    "\n&7Class: &f" + classExp + " &7total exp, &f" + (classExp - totalExpAtLevel) + "&7/&f"
                    + (totalExpToLevel-totalExpAtLevel) + " &7exp to level &a(" + classProgressFormatted + "%)" +
                    "\n&7Profession: &f" + profExp + " &7total exp, &f" + (profExp - profExpAtLevel) + "&7/&f"
                    + (profTotalExpToLevel-profExpAtLevel) + " &7to level &a(" + profProgress + "%)"));
            sender.sendMessage("");
        }
    }
}
