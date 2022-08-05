package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@CommandAlias("help")
public class HelpCMD extends BaseCommand {
    private static final Set<String> COMMANDS = new HashSet<String>() {{
        add("exp");
        add("map");
        add("vote");
    }};

    private static final Set<String> HELP_PATHS = new LinkedHashSet<String>() {{
        add("commands");
        add("classes");
        add("professions");
    }};

    public HelpCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("helpPath", context -> HELP_PATHS);
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("commands", context -> COMMANDS);
    }

    @CatchUnknown
    @Default
    public void onCommand(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }
        Player player = (Player) commandSender;
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player,
                ChatColor.GOLD + "«« " +
                        ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "RUNIC HELP MENU " +
                        ChatColor.GOLD + "»»");
        ChatUtils.sendCenteredMessage(player, ChatColor.DARK_GREEN + "For more help, visit " + ChatColor.GREEN + "https://runicrealms.com/");
        ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "Select one of the following paths with /help [path]");
        StringBuilder helpPaths = new StringBuilder();
        for (String path : HELP_PATHS) {
            helpPaths.append(path).append(" ");
        }
        ChatUtils.sendCenteredMessage(player, ChatColor.RED + "" + helpPaths);
        player.sendMessage("");
    }

    @Subcommand("commands")
    @Syntax("<helpPath>")
    @CommandCompletion("@helpPaths")
    public void onCommandCommands(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }
        Player player = (Player) commandSender;
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player,
                ChatColor.GOLD + "«« " +
                        ChatColor.GOLD + ChatColor.BOLD + "COMMANDS " +
                        ChatColor.GOLD + "»»");
        player.sendMessage("");
        player.sendMessage(ColorUtil.format("&eYour character's class can be seen on the scoreboard."));
        player.sendMessage(ColorUtil.format("&eIt determines your fighting style, available armor and weaponry."));
        player.sendMessage(ColorUtil.format("&eThe five classes are: &aArcher, Cleric, Mage, Rogue, or Warrior."));
        player.sendMessage(ColorUtil.format("&eAt lv. 10, you can learn abilities from one of three subclasses."));
        player.sendMessage(ColorUtil.format("&eFor example, the mage can learn &dfrost, fire, or shadow magic."));
    }

    @Subcommand("classes")
    @Syntax("<helpPath>")
    @CommandCompletion("@helpPaths")
    public void onCommandClasses(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }
        Player player = (Player) commandSender;
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player,
                ChatColor.GOLD + "«« " +
                        ChatColor.GOLD + ChatColor.BOLD + "CLASSES " +
                        ChatColor.GOLD + "»»");
        player.sendMessage("");
        player.sendMessage(ColorUtil.format("&eYour character's class can be seen on the scoreboard."));
        player.sendMessage(ColorUtil.format("&eIt determines your fighting style, available armor and weaponry."));
        player.sendMessage(ColorUtil.format("&eThe five classes are: &aArcher, Cleric, Mage, Rogue, or Warrior."));
        player.sendMessage(ColorUtil.format("&eAt lv. 10, you can learn abilities from one of three subclasses."));
        player.sendMessage(ColorUtil.format("&eFor example, the mage can learn &dfrost, fire, or shadow magic."));
    }

    @Subcommand("professions")
    @Syntax("<helpPath>")
    @CommandCompletion("@helpPaths")
    public void onCommandProfessions(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be run by a player!");
            return;
        }
        Player player = (Player) commandSender;
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player,
                ChatColor.GOLD + "«« " +
                        ChatColor.GOLD + ChatColor.BOLD + "PROFESSIONS " +
                        ChatColor.GOLD + "»»");
        player.sendMessage("");
        player.sendMessage(ColorUtil.format("&eYour character's class can be seen on the scoreboard."));
        player.sendMessage(ColorUtil.format("&eIt determines your fighting style, available armor and weaponry."));
        player.sendMessage(ColorUtil.format("&eThe five classes are: &aArcher, Cleric, Mage, Rogue, or Warrior."));
        player.sendMessage(ColorUtil.format("&eAt lv. 10, you can learn abilities from one of three subclasses."));
        player.sendMessage(ColorUtil.format("&eFor example, the mage can learn &dfrost, fire, or shadow magic."));
    }
}
