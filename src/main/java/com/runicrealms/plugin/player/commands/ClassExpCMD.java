package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.shop.BoostCMD;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ClassExpCMD implements SubCommand {

    private RunicGiveSC giveItemSC;
    private static final int LV_BUFFER = 10;
    private static double PARTY_BONUS = 15;
    private static int RANGE = 100;

    public ClassExpCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive exp [player] [amount] [x] [y] [z] [levelOfMob]
        // runicgive exp [player] [amount] [uuidOfMob] [mobLevel]
        // runicgive exp [player] [amount] [quest]
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        int plLv = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel();

        // skip all other calculations for quest exp
        if (args.length == 4) {
            int exp = Integer.parseInt(args[2]);
            PlayerLevelUtil.giveExperience(pl, exp);
            return;
        }

        // if the player doesn't have a party or they're in there by themself, give them regular exp.
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null
                || RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).getPartySize() < 2) {
            if (args.length != 7) {
                int exp = Integer.parseInt(args[2]);
                PlayerLevelUtil.giveExperience(pl, exp);
            } else {
                int mobLv = Integer.parseInt(args[6]);
                double percent = BoostCMD.getCombatExperienceBoost()/100;
                int exp = Integer.parseInt(args[2]);
                int boost = (int) percent*exp;
                exp += boost;
                Location loc = new Location(pl.getWorld(), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                ChatColor expColor = ChatColor.WHITE;
                if (mobLv > (plLv+LV_BUFFER) || mobLv < (plLv-LV_BUFFER)) {
                    exp = 0;
                    expColor = ChatColor.RED;
                }
                PlayerLevelUtil.giveExperience(pl, exp);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ " + expColor + exp + " &7exp"), 0, 2.5, 0);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName()), 0, 2.25, 0);
            }

        // otherwise, apply party exp bonus
        } else {

            Party party = RunicCore.getPartyManager().getPlayerParty(pl);
            double boostPercent = BoostCMD.getCombatExperienceBoost()/100;
            int exp = Integer.parseInt(args[2]);
            int boost = (int) boostPercent*exp;
            exp += boost;
            int originalExp = Integer.parseInt(args[2]) + boost;
            double percent = PARTY_BONUS / 100;
            int extraAmt = (int) (exp * percent);
            if (extraAmt < 1) {
                extraAmt = 1;
            }
            exp += extraAmt;

            int nearbyMembers = 0;
            for (Player member : party.getPlayerMembers()) {
                if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;
                if (pl.getLocation().distance(member.getLocation()) < RANGE) {
                    nearbyMembers += 1;
                }
            }

            for (Player member : party.getPlayerMembers()) {
                if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;
                if (pl.getLocation().distance(member.getLocation()) < RANGE) {
                    int mobLv = Integer.parseInt(args[6]);
                    int memberLv = RunicCore.getCacheManager().getPlayerCache(member.getUniqueId()).getClassLevel();
                    if (mobLv > (memberLv+LV_BUFFER) || mobLv < (memberLv-LV_BUFFER)) {
                        PlayerLevelUtil.giveExperience(member, 0);
                        Location loc = new Location(member.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                        HologramUtil.createStaticHologram(member, loc.clone(), ColorUtil.format("&7+ &c0 &7exp"), 0, 2.9, 0, true);
                    } else {
                        PlayerLevelUtil.giveExperience(member, (exp / nearbyMembers));
                    }
                }
            }

            if (args.length == 7) {
                Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ " + ChatColor.WHITE + originalExp + "&a(+" + extraAmt + ") &7exp"), 0, 2.6, 0);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName() + "&7's Party"), 0, 2.3, 0);
            }
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if (args.length == 3 || args.length == 4 || args.length == 7) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount] ([x] [y] [z]) [level]");
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount] (quest)");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.generateitem";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
        //return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
    }
}
