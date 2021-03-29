package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.utilities.NametagUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.Set;

@SuppressWarnings("deprecation")
public class ScoreboardHandler implements Listener {

    public ScoreboardHandler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player pl : RunicCore.getCacheManager().getLoadedPlayers()) {
                    updateSideInfo(pl);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100L, 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(CharacterLoadEvent e) {
        Player pl = e.getPlayer();
        createScoreboard(pl);
        NametagUtil.updateNametag(pl);
    }

    private void createScoreboard(Player pl){

        // create our scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // setup side scoreboard
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(ChatColor.LIGHT_PURPLE + "     §lRunic Realms     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        // set the board!
        pl.setScoreboard(board);
    }

    private void updateSideInfo(Player pl){

        Scoreboard board = pl.getScoreboard();
        Objective sidebar = board.getObjective("sidebar");

        // ensure the scoreboard objective exists
        if (sidebar == null)
            return;

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
        Score characterInfo = sidebar.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + "Character");
        characterInfo.setScore(7);

        updatePlayerInfo(pl);

        // setup side health display
        Score health = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(healthAsString(pl));
        health.setScore(2);
        Score mana = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(manaAsString(pl));
        mana.setScore(1);
        if (!shieldAsString(pl).equals("")) {
            Score shield = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(shieldAsString(pl));
            shield.setScore(0);
        }
    }

    private void updatePlayerInfo(Player pl) {

        Scoreboard board = pl.getScoreboard();
        Objective sidebar = board.getObjective("sidebar");

        // ensure the scoreboard objective exists
        if (sidebar == null) { return; }

        Score playerClass = sidebar.getScore(playerClass(pl));
        playerClass.setScore(6);
        Score playerProfession = sidebar.getScore(playerProf(pl));
        playerProfession.setScore(5);
        Score playerGuild = sidebar.getScore(playerGuild(pl));
        playerGuild.setScore(4);
    }

    private String healthAsString(Player pl) {
        int currentHealth = (int) pl.getHealth();
        int maxHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return ChatColor.DARK_RED + "❤ " + ChatColor.RED + currentHealth + " §e/ " + ChatColor.RED + maxHealth + " (Health)";
    }

    private String manaAsString(Player pl) {
        int mana = RunicCore.getRegenManager().getCurrentManaList().get(pl.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(pl).getMaxMana();
        return ChatColor.DARK_AQUA + "✸ " + mana + " §e/ " + ChatColor.DARK_AQUA + maxMana + " (Mana)";
    }

    private String shieldAsString(Player player) {
        double shield;
        try {
            shield = RunicCore.getCombatManager().getShieldedPlayers().get(player.getUniqueId());
        } catch (NullPointerException e) {
            return "";
        }
        return ChatColor.WHITE + "■ " + (int) shield + " (Shield)";
    }

    private String playerClass(Player pl) {
        String className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassLevel();
        String display;
        if (className == null) {
            display = ChatColor.YELLOW + "Class: " + ChatColor.GREEN + "None";
        } else {
            display = ChatColor.YELLOW + "Class: " + ChatColor.GREEN + className;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + className + ChatColor.YELLOW + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    private String playerProf(Player pl) {
        String profName = RunicCore.getCacheManager().getPlayerCaches().get(pl).getProfName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCaches().get(pl).getProfLevel();
        String display;
        if (profName == null) {
            display = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + "None";
        } else {
            display = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + profName;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + profName + ChatColor.YELLOW + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    private String playerGuild(Player pl) {
        try {
            String display;
            String guild = RunicCore.getCacheManager().getPlayerCaches().get(pl).getGuild();
            if (!guild.toLowerCase().equals("none")) {
                display = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + guild;
            } else {
                display = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + "None";
            }
            return display;
        } catch (Exception e) {
            return ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + "None";
        }
    }
}