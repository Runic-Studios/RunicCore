package us.fortherealm.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Set;

public class ScoreboardUtil {

    public ScoreboardUtil() {
    }

    public void setupScoreboard(final Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        final Objective sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
        final Objective healthBar = scoreboard.registerNewObjective("healthBar", "health");

        sidebar.setDisplayName(ChatColor.YELLOW + "     §lFor the Realm     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        healthBar.setDisplayName(ChatColor.RED + "❤");
        healthBar.setDisplaySlot(DisplaySlot.BELOW_NAME);

        player.setScoreboard(scoreboard);
        updateSideScoreboard(player);
        updateHealthBar(player);
    }

    public void updateSideScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective sidebar = scoreboard.getObjective("sidebar");

        Set<String> entries;

        entries = scoreboard.getEntries();

        for(String entry : entries)
        {
            scoreboard.resetScores(entry);
        }

        Score blankSpace8 = sidebar.getScore("§7");//cuz lines cannot be identical we add a random color code
        blankSpace8.setScore(7);

        Score characterInfo = sidebar.getScore(ChatColor.YELLOW + "§lCharacter");
        characterInfo.setScore(6);

        Score playerClass = sidebar.getScore(ChatColor.GRAY + "Class: " + ChatColor.GREEN + "None");
        playerClass.setScore(5);

        Score playerProfession = sidebar.getScore(ChatColor.GRAY + "Prof: " + ChatColor.GREEN + "None");
        playerProfession.setScore(4);

        Score playerGuild = sidebar.getScore(ChatColor.GRAY + "Guild: " + ChatColor.GREEN + "None");
        playerGuild.setScore(3);

        Score blankSpace4 = sidebar.getScore("");
        blankSpace4.setScore(2);

        Score healthDisplay = sidebar.getScore(ChatColor.DARK_RED + "❤ " + ChatColor.RED + ((int) player.getHealth())
                + " §7/ " + ChatColor.RED + ((int) player.getMaxHealth()));
        healthDisplay.setScore(1);
    }

    public void updateHealthBar (Player player) {
        org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
        Objective healthBar = scoreboard.getObjective("healthBar");

        for (Player online : Bukkit.getOnlinePlayers()) {
            Score score = healthBar.getScore(online);
            score.setScore((int) online.getHealth());
        }
    }
}
