package us.fortherealm.plugin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;
import us.fortherealm.plugin.Main;
import java.util.Set;

// TODO: known bug: throws NPE on a player first-join

class ScoreboardManager {

    private Plugin plugin = Main.getInstance();

    void setupScoreboard(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // don't double register objectives
        if (board.getObjective("healthbar") != null) {
            board.getObjective("healthbar").unregister();
        }

        // setup health below the nameplate
        Objective healthbar = board.registerNewObjective("healthbar", "health");
        healthbar.setDisplayName(ChatColor.RED + "❤");
        healthbar.setDisplaySlot(DisplaySlot.BELOW_NAME);

        // don't double register objectives
        if (board.getObjective("sidebar") != null) {
            board.getObjective("sidebar").unregister();
        }

        // setup side scoreboard
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(ChatColor.YELLOW + "     §lFor the Realm     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        // set the board!
        player.setScoreboard(board);
        updateSideScoreboard(player);

        // each time a player logs in, set the healthbar for all other players
        for (Player online : Bukkit.getOnlinePlayers()) {
            Score score = healthbar.getScore(online.getName());
            score.setScore((int) online.getHealth());
        }
    }

    void updateSideScoreboard(Player victim) {

        // grab the objective
        Scoreboard scoreboard = victim.getScoreboard();
        Objective sidebar = scoreboard.getObjective("sidebar");

        // initialize our variables
        Set<String> entries;
        entries = scoreboard.getEntries();

        // reset entries
        for (String entry : entries) {
            scoreboard.resetScores(entry);
        }

        //side bar defaults (lines cannot be identical we add a random color code to a blank line)
        Score blankSpace8 = sidebar.getScore("§1");
        blankSpace8.setScore(7);
        Score characterInfo = sidebar.getScore(ChatColor.YELLOW + "§lCharacter");
        characterInfo.setScore(6);
        Score playerClass = sidebar.getScore(ChatColor.GRAY + "Class: " + ChatColor.GREEN + "None");
        playerClass.setScore(5);
        Score playerProfession = sidebar.getScore(ChatColor.GRAY + "Prof: " + ChatColor.GREEN + "None");
        playerProfession.setScore(4);
        Score playerGuild = sidebar.getScore(ChatColor.GRAY + "Guild: " + ChatColor.GREEN + "None");
        playerGuild.setScore(3);
        Score blankSpace4 = sidebar.getScore("§2");
        blankSpace4.setScore(2);
        Score healthDisplay = sidebar.getScore(ChatColor.DARK_RED + "❤ " + ChatColor.RED + ((int) victim.getHealth())
                + " §7/ " + ChatColor.RED + ((int) victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        healthDisplay.setScore(1);

    }
}
