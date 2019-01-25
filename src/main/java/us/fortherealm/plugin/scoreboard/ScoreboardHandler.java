package us.fortherealm.plugin.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import us.fortherealm.plugin.Main;

import java.util.Set;

public class ScoreboardHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {

        new BukkitRunnable() {
            @Override
            public void run() {
                createScoreboard(e.getPlayer());
                updateSideInfo(e.getPlayer());
            }
        }.runTaskLater(Main.getInstance(), 20L);
    }

    public void createScoreboard(Player pl){

        // create our scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // setup side scoreboard
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(ChatColor.LIGHT_PURPLE + "     §lFor The Realm     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

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
        blankSpaceSeven.setScore(8);
        Score blankSpaceTwo = sidebar.getScore("§2");
        blankSpaceTwo.setScore(3);

        // set side board header
        Score characterInfo = sidebar.getScore(ChatColor.GRAY + "" + ChatColor.BOLD + "Character");
        characterInfo.setScore(7);

        // TODO: update info for guild
        updatePlayerInfo(pl);

        // setup side health display
        Score health = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(healthAsString(pl));
        health.setScore(2);
        Score mana = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(manaAsString(pl));
        mana.setScore(1);
    }

    public void updatePlayerInfo(Player pl) {

        Scoreboard board = pl.getScoreboard();
        Objective sidebar = board.getObjective("sidebar");

        // ensure the scoreboard objective exists
        if (sidebar == null) { return; }

        Score playerClass = sidebar.getScore(playerClass(pl));
        playerClass.setScore(6);
        Score playerProfession = sidebar.getScore(playerProf(pl));
        playerProfession.setScore(5);
        Score playerGuild = sidebar.getScore(playerGuild());
        playerGuild.setScore(4);
    }

    private String healthAsString(Player pl) {
        int currentHealth = (int) Math.round(pl.getHealth());
        int maxHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return ChatColor.DARK_RED + "❤ " + ChatColor.RED + currentHealth + " §7/ " + ChatColor.RED + maxHealth;
    }

    private String manaAsString(Player pl) {
        int mana = Main.getManaManager().getCurrentManaList().get(pl.getUniqueId());
        int maxMana = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        return ChatColor.DARK_AQUA + "✸ " + mana + " §7/ " + ChatColor.DARK_AQUA + maxMana;
    }

    private String playerClass(Player pl) {
        String className = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        int currentLevel = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");
        String display;
        if (className == null) {
            display = ChatColor.GRAY + "Class: " + ChatColor.GREEN + "None";
        } else {
            display = ChatColor.GRAY + "Class: " + ChatColor.GREEN + className;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + className + ChatColor.GRAY + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    private String playerProf(Player pl) {
        String profName = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.prof.name");
        int currentLevel = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");
        String display;
        if (profName == null) {
            display = ChatColor.GRAY + "Prof: " + ChatColor.GREEN + "None";
        } else {
            display = ChatColor.GRAY + "Prof: " + ChatColor.GREEN + profName;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + profName + ChatColor.GRAY + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    private String playerGuild() {
        String plGuild = ChatColor.GRAY + "Guild: " + ChatColor.GREEN + "None";
        return plGuild;
    }
}