package com.runicrealms.plugin.donor.boost.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.donor.boost.api.Boost;
import com.runicrealms.plugin.donor.boost.api.BoostExperienceType;
import com.runicrealms.plugin.donor.boost.ui.BoostsUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("boost")
@Private
public class BoostCommand extends BaseCommand {

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean isDouble(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @CatchUnknown
    @Default
    public void onCommand(Player player) {
        player.openInventory(new BoostsUI(player).getInventory());
    }

    @Subcommand("custom")
    @Conditions("is-op")
    public void onCommandCustom(Player player, String[] args) {
        if (args.length != 4 || !isInt(args[2]) || !isDouble(args[3]) || BoostExperienceType.getFromIdentifier(args[1]) == null) {
            player.sendMessage(ChatColor.RED + "Please use format: /boost custom <booster-name> <experience-type> <duration-minutes> <multiplier-decimal>");
            player.sendMessage(ChatColor.RED + "The booster name may contain underscores in place of spaces. Experience type is crafting, combat, or gathering.");
            return;
        }
        String name = ColorUtil.format(args[0].replaceAll("_", " "));
        int duration = Integer.parseInt(args[2]);
        double multiplier = Double.parseDouble(args[3]);
        BoostExperienceType experienceType = BoostExperienceType.getFromIdentifier(args[1]);
        Boost boost = new Boost() {
            @Override
            public int getDuration() {
                return duration;
            }

            @Override
            public double getAdditionalMultiplier() {
                return multiplier;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public BoostExperienceType getExperienceType() {
                return experienceType;
            }
        };
        RunicCore.getBoostAPI().activateBoost(player, boost);
    }

}
