package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@CommandAlias("help")
public class HelpCMD extends BaseCommand {
    private static final Map<String, String> COMMANDS = new HashMap<String, String>() {{
        put("exp", "View your character's progress");
        put("guild", "Create a guild ");
        put("map", "Get a link to the world map");
        put("party", "Create a party and group with friends!");
        put("vote", "Vote for the server and earn rewards!");
    }};

    private static final Set<String> HELP_PATHS = new LinkedHashSet<String>() {{
        add("commands");
        add("classes");
        add("professions");
    }};

    public HelpCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("helpPath", context -> HELP_PATHS);
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("commands", context -> COMMANDS.keySet());
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
        for (String command : COMMANDS.keySet()) {
            player.sendMessage(ColorUtil.format("&c/" + command + " &e- " + COMMANDS.get(command)));
        }
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
        player.sendMessage(ColorUtil.format("&eYour character's profession can be seen on the scoreboard."));
        player.sendMessage(ColorUtil.format("&eIt determines which items you can craft."));
        player.sendMessage(ColorUtil.format("&eThe five professions are: &aAlchemist, Blacksmith, Enchanter, Hunter, or Jeweler."));
        player.sendMessage(ColorUtil.format("&eGathering skills are account-wide and can be seen from the inventory menu."));
        player.sendMessage(ColorUtil.format("&eCombine crafting and gathering to create powerful items!"));
    }
}
