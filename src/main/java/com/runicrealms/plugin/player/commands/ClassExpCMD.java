package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ClassExpCMD implements SubCommand {

    private RunicGiveSC giveItemSC;
    private static final int LV_BUFFER = 10;
    private static double PARTY_BONUS = 25;
    private static int RANGE = 100;

    public ClassExpCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive exp [player] [amount] [x] [y] [z] [levelOfMob]
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
                if (mobLv > (plLv+LV_BUFFER)) {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.RED + "This mob is too far outside of your level range for you to receive exp!");
                    return;
                }
                int exp = Integer.parseInt(args[2]);
                PlayerLevelUtil.giveExperience(pl, exp);
                Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ &f" + exp + " &7exp"), 0, 2.5, 0);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName()), 0, 2.25, 0);
            }

        // otherwise, apply party exp bonus
        } else {

            Party party = RunicCore.getPartyManager().getPlayerParty(pl);
            int exp = Integer.parseInt(args[2]);
            int originalExp = Integer.parseInt(args[2]);
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
                if (pl.getLocation().distance(member.getLocation()) < RANGE) {
                    if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;
                    int mobLv = Integer.parseInt(args[6]);
                    if (mobLv > (plLv+LV_BUFFER)) {
                        pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
                        pl.sendMessage(ChatColor.RED + "This mob is too far outside of your level range for you to receive exp!");
                        continue;
                    }
                    PlayerLevelUtil.giveExperience(member, (exp / nearbyMembers));
                }
            }

            if (args.length == 7) {
                Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ &f" + originalExp + "&a(+" + extraAmt + ") &7exp"), 0, 2.5, 0);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName() + "&7's Party"), 0, 2.25, 0);
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
