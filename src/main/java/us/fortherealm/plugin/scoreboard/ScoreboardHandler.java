package us.fortherealm.plugin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import us.fortherealm.plugin.Main;

import java.util.Set;

public class ScoreboardHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        // if our player is new, give the server 1s to update their total hp before the scoreboard is created
        if (!e.getPlayer().hasPlayedBefore()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    createScoreboard(e.getPlayer());
                    updateSideInfo(e.getPlayer());
                    updateHealthbar(e.getPlayer());
                }
            }.runTaskLater(Main.getInstance(), 20L);
        } else {
            createScoreboard(e.getPlayer());
            updateSideInfo(e.getPlayer());
            updateHealthbar(e.getPlayer());
        }
    }

    public void createScoreboard(Player pl){

        // create our scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // setup side scoreboard
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(ChatColor.LIGHT_PURPLE + "     §lFor The Realm     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        // setup health below the nameplate
        Objective healthbar = board.registerNewObjective("healthbar", "health");
        healthbar.setDisplayName(ChatColor.RED + "❤");
        healthbar.setDisplaySlot(DisplaySlot.BELOW_NAME);

        // set the board!
        pl.setScoreboard(board);
    }

    public void updateSideInfo(Player pl){

        Scoreboard board = pl.getScoreboard();
        Objective sidebar = board.getObjective("sidebar");

        // ensure the scoreboard objective exists
        if (sidebar == null) { return; }

        Set<String> entries;
        entries = pl.getScoreboard().getEntries();

        // reset entries to prevent duplication & flickering
        for (String entry : entries) {
            board.resetScores(entry);
        }

        // add pretty formatting to side board
        Score blankSpaceSeven = sidebar.getScore("§1");
        blankSpaceSeven.setScore(7);
        Score blankSpaceTwo = sidebar.getScore("§2");
        blankSpaceTwo.setScore(2);

        // set side board header
        Score characterInfo = sidebar.getScore(ChatColor.GREEN + "" + ChatColor.BOLD + "Character");
        characterInfo.setScore(6);

        // TODO: update info for prof, and guild
        updatePlayerInfo(pl);

        // setup side health display
        Score side = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(healthAsString(pl));
        side.setScore(1);
    }

    public void updateHealthbar(Player pl) {

        Objective healthbar = pl.getScoreboard().getObjective("healthbar");

        // ensure the scoreboard objective exists
        if (healthbar == null) { return; }

        // updates the health below the nameplate for all OTHER players
        for (Player online : Bukkit.getOnlinePlayers()) {
            Score score = healthbar.getScore(online.getName());
            score.setScore((int) online.getHealth());
        }
    }

    public void updatePlayerInfo(Player pl) {

        Scoreboard board = pl.getScoreboard();
        Objective sidebar = board.getObjective("sidebar");

        // ensure the scoreboard objective exists
        if (sidebar == null) { return; }

        Score playerClass = sidebar.getScore(playerClass(pl));
        playerClass.setScore(5);
        Score playerProfession = sidebar.getScore(playerProf());
        playerProfession.setScore(4);
        Score playerGuild = sidebar.getScore(playerGuild());
        playerGuild.setScore(3);
    }

    private String healthAsString(Player pl) {
        return ChatColor.DARK_RED + "❤ " + ChatColor.RED + ((int) Math.round(pl.getHealth()))
                + " §7/ " + ChatColor.RED + ((int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    private String playerClass(Player pl) {
        String className = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class");
        return ChatColor.GRAY + "Class: "+ ChatColor.GREEN + className + ChatColor.GRAY + " - " + ChatColor.GREEN + pl.getLevel();
    }

    private String playerProf() {
        String plProf = ChatColor.GRAY + "Prof: " + ChatColor.GREEN + "None" + ChatColor.GRAY + " - " + ChatColor.GREEN + "0";
        return plProf;
    }

    private String playerGuild() {
        String plGuild = ChatColor.GRAY + "Guild: " + ChatColor.GREEN + "None";
        return plGuild;
    }
}
