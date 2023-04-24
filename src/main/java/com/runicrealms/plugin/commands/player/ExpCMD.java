package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.CoreCharacterData;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@CommandAlias("experience|exp")
public class ExpCMD extends BaseCommand {

    @CatchUnknown
    @Default
    public void onCommandClass(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only a player may run this command!");
            return;
        }
        Player player = (Player) commandSender;
        int classLevel = player.getLevel();
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
        CoreCharacterData characterData = RunicCore.getDataAPI().getCorePlayerDataMap().get(player.getUniqueId()).getCharacter(slot);
        int classExp = characterData.getExp();
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(classLevel);
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(classLevel + 1);
        double proportion = (double) (classExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel) * 100;
        NumberFormat toDecimal = new DecimalFormat("#0.00");
        String classProgressFormatted = toDecimal.format(proportion);
        player.sendMessage("");
        player.sendMessage(ColorUtil.format
                ("&a&lPlayer " + player.getName() +
                        "\n&7Class: &f" + classExp + " &7total exp, &f" + (classExp - totalExpAtLevel) + "&7/&f"
                        + (totalExpToLevel - totalExpAtLevel) + " &7exp to level &a(" + classProgressFormatted + "%)"));
        player.sendMessage("");
    }

}
