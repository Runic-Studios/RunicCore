package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.model.CoreCharacterData;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.utilities.NametagHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.runicrealms.plugin.classes.SelectClass.setPlayerClass;

@CommandAlias("set")
@CommandPermission("runic.op")
public class SetCMD extends BaseCommand {

    public SetCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("online", context -> {
            Set<String> onlinePlayers = new HashSet<>();
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
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
        player.setLevel(0);
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        CoreCharacterData characterData = RunicCore.getPlayerDataAPI().getCorePlayerData(player.getUniqueId()).getCharacter(slot);
        characterData.setLevel(0);
        characterData.setExp(0);
        characterData.setClassType(CharacterClass.getFromName(classString));
        PlayerLevelUtil.giveExperience(player, 0); // Saves to jedis

        // Upload cached class info
        RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().getMap().put
                (
                        player.getUniqueId(),
                        Pair.pair(slot, CharacterClass.getFromName(classString))
                ); // Now we always know which character is playing

        // Upload scoreboard, nametag
        RunicCore.getScoreboardAPI().setupScoreboard(player);
        NametagHandler.updateNametag(player, slot);
    }

    // set hearthstone [player] [location]

    @Subcommand("hearthstone|hs")
    @CommandCompletion("@online @locations")
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

    // set hunger [player] [hunger]

    @Subcommand("hunger")
    @Syntax("<player> <hunger>")
    @CommandCompletion("@online @nothing")
    @Conditions("is-console-or-op")
    public void onCommandHunger(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: set hunger [player] [hunger]");
            return;
        }
        Player player;
        int hunger;
        player = Bukkit.getPlayer(args[0]);
        hunger = Integer.parseInt(args[1]);
        if (player == null) return;
        player.setFoodLevel(hunger);
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
        UUID uuid = player.getUniqueId();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        int expAtLevel = PlayerLevelUtil.calculateTotalExp(level) + 1;
        // Reset values to 0 in-memory
        player.setLevel(0);
        CoreCharacterData characterData = RunicCore.getPlayerDataAPI().getCorePlayerData(uuid).getCharacter(slot);
        characterData.setLevel(0);
        characterData.setExp(0);
        PlayerLevelUtil.giveExperience(player, expAtLevel);
    }

}
