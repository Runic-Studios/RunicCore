package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.professions.api.RunicProfessionsAPI;
import com.runicrealms.plugin.professions.gathering.GatherPlayer;
import com.runicrealms.plugin.professions.gathering.GatheringSkill;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.redis.RedisField;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.runicrealms.plugin.classes.SelectClass.setPlayerClass;
import static com.runicrealms.plugin.classes.SelectClass.setupCache;

@CommandAlias("set")
public class SetCMD extends BaseCommand {

    public SetCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("online", context -> {
            Set<String> onlinePlayers = new HashSet<>();
            for (UUID uuid : RunicCoreAPI.getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                onlinePlayers.add(player.getName());
            }
            return onlinePlayers;
        });
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
    @Syntax("<player> <class>")
    @CommandCompletion("@online @classes")
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
            player.sendMessage(ChatColor.AQUA + "You have changed your hearthstone location to " + CityLocation.getFromIdentifier(location).getDisplay() + "!");
            player.getInventory().setItem(8, CityLocation.getFromIdentifier(location).getItemStack());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // set level [player] [level]

    @Subcommand("level")
    @Syntax("<player> <level>")
    @CommandCompletion("@online @nothing")
    @Conditions("is-console-or-op")
    public void onCommandLevel(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set level [player] [level]");
            return;
        }
        Player player;
        int level;
        player = Bukkit.getPlayer(args[0]);
        level = Integer.parseInt(args[1]);
        if (player == null) return;
        int expAtLevel = PlayerLevelUtil.calculateTotalExp(level) + 1;
        int expectedLv = PlayerLevelUtil.calculateExpectedLv(expAtLevel);
        player.setLevel(0);
        RunicCoreAPI.setRedisValue(player, RedisField.CLASS_EXP, String.valueOf(0));
        PlayerLevelUtil.giveExperience(player, expAtLevel);
        // RunicCore.getCacheManager().getPlayerCaches().get(player).setClassLevel(expectedLv);
        /*
        IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the class level!
        */
        // RunicCore.getCacheManager().getPlayerCaches().get(player).setClassExp(expAtLevel);
    }

    // set proflevel [player] [level]

    @Subcommand("proflevel")
    @Syntax("<player> <level>")
    @CommandCompletion("@online @nothing")
    @Conditions("is-console-or-op")
    public void onCommandProfLevel(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set proflevel {player} {level}");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        RunicCoreAPI.setRedisValue(player, RedisField.PROF_LEVEL, args[1]);
        // ----------------------
        // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
        int expAtLevel = ProfExpUtil.calculateTotalExperience(Integer.parseInt(args[1]));
        // ----------------------
        RunicCoreAPI.setRedisValue(player, RedisField.PROF_EXP, String.valueOf(expAtLevel));
    }

    // set gatheringlevel [player] [skill] [level]

    @Subcommand("gatheringlevel")
    @Syntax("<player> <skill> <level>")
    @CommandCompletion("@online @gatheringSkills @nothing")
    @Conditions("is-console-or-op")
    public void onCommandGatheringLevel(CommandSender commandSender, String[] args) {
        if (args.length != 3) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set gatheringlevel [player] [skill] [level]");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        GatherPlayer gatherPlayer = RunicProfessionsAPI.getGatherPlayer(player.getUniqueId());
        GatheringSkill gatheringSkill = GatheringSkill.getFromIdentifier(args[1]);
        if (gatheringSkill == null) return;
        int level = Integer.parseInt(args[2]);
        gatherPlayer.setGatheringLevel(gatheringSkill, level);
        // ----------------------
        // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
        int expAtLevel = ProfExpUtil.calculateTotalExperience(level);
        // ----------------------
        gatherPlayer.setGatheringExp(gatheringSkill, expAtLevel);
    }

}
