package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.hearthstone.HearthstoneLocation;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

import static com.runicrealms.plugin.classes.SelectClass.setPlayerClass;
import static com.runicrealms.plugin.classes.SelectClass.setupCache;

@CommandAlias("set")
public class SetCMD extends BaseCommand {

    public SetCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("classes", context -> {
            Set<String> classes = new HashSet<>();
            classes.add("archer");
            classes.add("cleric");
            classes.add("mage");
            classes.add("rogue");
            classes.add("warrior");
            return classes;
        });
    }

    @Subcommand("class")
    @CommandCompletion("@players @classes")
    @Conditions("is-console-or-op")
    public void onCommandClass(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set class {player} {class} or set class {class}");
            return;
        }
        Player player;
        String classString;
        if (args.length == 1 && commandSender instanceof Player) {
            player = (Player) commandSender;
            classString = args[0].toLowerCase();
        } else {
            player = Bukkit.getPlayer(args[0]);
            classString = args[1].toLowerCase();
        }
        try {
            if (!(classString.equals("archer")
                    || classString.equals("cleric")
                    || classString.equals("mage")
                    || classString.equals("rogue")
                    || classString.equals("warrior"))) {
                player.sendMessage(ChatColor.RED
                        + "Available classes: archer, cleric, mage, rogue, warrior");
                return;
            }
            String formattedStr = classString.substring(0, 1).toUpperCase() + classString.substring(1);
            setPlayerClass(player, formattedStr, true);
            setupCache(player, formattedStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subcommand("hearthstone|hs")
    @Conditions("is-console-or-op")
    public void onCommandHearthstone(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set hearthstone {player} {location}");
            return;
        }
        try {
            Player player = Bukkit.getPlayer(args[0]);
            String location = args[1];
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.sendMessage(ChatColor.AQUA + "You have changed your hearthstone location to " + HearthstoneLocation.getFromIdentifier(location).getDisplay() + "!");
            player.getInventory().setItem(8, HearthstoneLocation.getFromIdentifier(location).getItemStack());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subcommand("level")
    @Conditions("is-console-or-op")
    public void onCommandLevel(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set level {player} {level} or set level {level}");
            return;
        }
        Player player;
        int level;
        if (args.length == 1 && commandSender instanceof Player) {
            player = (Player) commandSender;
            level = Integer.parseInt(args[0]);
        } else {
            player = Bukkit.getPlayer(args[0]);
            level = Integer.parseInt(args[1]);
        }
        try {
            int expAtLevel = PlayerLevelUtil.calculateTotalExp(level) + 1;
            int expectedLv = PlayerLevelUtil.calculateExpectedLv(expAtLevel);
            player.setLevel(0);
            RunicCore.getCacheManager().getPlayerCaches().get(player).setClassExp(0);
            PlayerLevelUtil.giveExperience(player, expAtLevel);
            RunicCore.getCacheManager().getPlayerCaches().get(player).setClassLevel(expectedLv);
        /*
        IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the class level!
        */
            RunicCore.getCacheManager().getPlayerCaches().get(player).setClassExp(expAtLevel);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Subcommand("proflevel")
    @Conditions("is-console-or-op")
    public void onCommandProfLevel(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set proflevel {player} {level} or set proflevel {level}");
            return;
        }
        if (args.length == 1 && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            RunicCore.getCacheManager().getPlayerCaches().get(player).setProfLevel(Integer.parseInt(args[0]));
            // ----------------------
            // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
            int expAtLevel = ProfExpUtil.calculateTotalExperience(Integer.parseInt(args[0]));
            // ----------------------
            RunicCore.getCacheManager().getPlayerCaches().get(player).setProfExp(expAtLevel);

            String profName = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfName();
            if (Integer.parseInt(args[0]) == 30) {

                player.sendMessage("\n");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ChatUtils.sendCenteredMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "PROFESSION UPGRADE!");
                ChatUtils.sendCenteredMessage(player, ChatColor.WHITE + "" + ChatColor.BOLD + "You are now a Refined " + profName + "!");
                ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "        Your crafted goods have become more powerful!");
                player.sendMessage("\n");

            } else if (Integer.parseInt(args[0]) == 50) {

                player.sendMessage("\n");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ChatUtils.sendCenteredMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "PROFESSION UPGRADE!");
                ChatUtils.sendCenteredMessage(player, ChatColor.WHITE + "" + ChatColor.BOLD + "You are now an Artisan " + profName + "!");
                ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "        Your crafted goods have become more powerful!");
                player.sendMessage("\n");

            }

        } else if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) return;
            RunicCore.getCacheManager().getPlayerCaches().get(player).setProfLevel(Integer.parseInt(args[1]));
            // ----------------------
            // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
            int expAtLevel = ProfExpUtil.calculateTotalExperience(Integer.parseInt(args[1]));
            // ----------------------
            RunicCore.getCacheManager().getPlayerCaches().get(player).setProfExp(expAtLevel);
        }
    }

}
