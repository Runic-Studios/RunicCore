package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.utilities.NametagUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.*;

public class ScoreboardHandler implements Listener {

    // basic info
    private static final String CLASS_TEAM_STRING = "c";
    private static final String CLASS_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.DARK_GREEN;
    private static final String PROF_TEAM_STRING = "p";
    private static final String PROF_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.GREEN;
    private static final String GUILD_TEAM_STRING = "g";
    private static final String GUILD_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.GOLD;
    // combat team info
    private static final String HEALTH_TEAM_STRING = "h";
    private static final String HEALTH_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.RED;
    private static final String MANA_TEAM_STRING = "m";
    private static final String MANA_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.AQUA;

    public ScoreboardHandler() {
        // periodically update class, prof, guild info
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
                updatePlayerInfo(player, player.getScoreboard());
            }
        }, 100L, 20L);
        // periodically update combat info (much faster)
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
                updatePlayerCombatInfo(player, player.getScoreboard());
            }
        }, 100L, 4L);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerJoin(CharacterSelectEvent e) {
        Player player = e.getPlayer();
        setupScoreboard(player);
        NametagUtil.updateNametag(player);
    }

    /**
     * Set the scoreboard for the given player if they do not yet have one
     *
     * @param player to receive scoreboard
     */
    private void setupScoreboard(final Player player) {
        assert Bukkit.getScoreboardManager() != null;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("ServerName", "", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "     Runic Realms     ");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        // setup basic fields
        Score blankSpaceSeven = obj.getScore("§1");
        blankSpaceSeven.setScore(8);
        Score characterInfo = obj.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName());
        characterInfo.setScore(7);
        setupPlayerInfo(player, scoreboard, obj);
        updatePlayerInfo(player, scoreboard);
        Score blankSpaceTwo = obj.getScore("§2");
        blankSpaceTwo.setScore(3);
        // setup combat fields using teams to avoid flickering
        setupPlayerCombatInfo(player, scoreboard, obj);
        updatePlayerCombatInfo(player, scoreboard);
        player.setScoreboard(scoreboard);
    }

    /**
     * Initial setup for basic scoreboard fields
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player     who owns the scoreboard
     * @param scoreboard the scoreboard of the player
     * @param obj        the main scoreboard objective defined in 'setupScoreboard'
     */
    private void setupPlayerInfo(final Player player, final Scoreboard scoreboard, final Objective obj) {
        String playerNameSubString = player.getName().length() > 16 ? player.getName().substring(0, 16) : player.getName();
        Team playerClass = scoreboard.registerNewTeam(playerNameSubString + CLASS_TEAM_STRING);
        playerClass.addEntry(CLASS_ENTRY_STRING);
        obj.getScore(CLASS_ENTRY_STRING).setScore(6);
        playerClass.setPrefix(playerClass(player)); // setup class prefix ONCE
        Team playerProf = scoreboard.registerNewTeam(playerNameSubString + PROF_TEAM_STRING);
        playerProf.addEntry(PROF_ENTRY_STRING);
        obj.getScore(PROF_ENTRY_STRING).setScore(5);
        Team playerGuild = scoreboard.registerNewTeam(playerNameSubString + GUILD_TEAM_STRING);
        playerGuild.addEntry(GUILD_ENTRY_STRING);
        obj.getScore(GUILD_ENTRY_STRING).setScore(4);
    }

    /**
     * Method used to keep scoreboard accurate during level-up, profession change, etc.
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player     who owns the scoreboard
     * @param scoreboard the scoreboard of the player
     */
    public void updatePlayerInfo(final Player player, final Scoreboard scoreboard) {
        try {
            String playerNameSubString = player.getName().length() > 16 ? player.getName().substring(0, 16) : player.getName();
            Team playerProf = scoreboard.getTeam(playerNameSubString + PROF_TEAM_STRING);
            assert playerProf != null;
            playerProf.setPrefix(playerProf(player));
            Team playerGuild = scoreboard.getTeam(playerNameSubString + GUILD_TEAM_STRING);
            assert playerGuild != null;
            playerGuild.setPrefix(playerGuild(player));
        } catch (NullPointerException e) {
            // wrapped in try-catch in-case scoreboard can't set up in time
        }
    }

    /**
     * Initial setup for player combat info using scoreboard teams to prevent flickering
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player     who owns the scoreboard
     * @param scoreboard the scoreboard of the player
     * @param obj        the main scoreboard objective defined in 'setupScoreboard'
     */
    private void setupPlayerCombatInfo(final Player player, final Scoreboard scoreboard, final Objective obj) {
        String playerNameSubString = player.getName().length() > 16 ? player.getName().substring(0, 16) : player.getName();
        Team playerHealth = scoreboard.registerNewTeam(playerNameSubString + HEALTH_TEAM_STRING);
        playerHealth.addEntry(HEALTH_ENTRY_STRING);
        obj.getScore(HEALTH_ENTRY_STRING).setScore(2);
        Team playerMana = scoreboard.registerNewTeam(playerNameSubString + MANA_TEAM_STRING);
        playerMana.addEntry(MANA_ENTRY_STRING);
        obj.getScore(MANA_ENTRY_STRING).setScore(1);
    }

    /**
     * Method used to keep scoreboard accurate on an async timer
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player     who owns the scoreboard
     * @param scoreboard the scoreboard of the player
     */
    private void updatePlayerCombatInfo(final Player player, final Scoreboard scoreboard) {
        try {
            String playerNameSubString = player.getName().length() > 16 ? player.getName().substring(0, 16) : player.getName();
            Team playerHealth = scoreboard.getTeam(playerNameSubString + HEALTH_TEAM_STRING);
            assert playerHealth != null;
            playerHealth.setPrefix(healthAsString(player));
            Team playerMana = scoreboard.getTeam(playerNameSubString + MANA_TEAM_STRING);
            assert playerMana != null;
            playerMana.setPrefix(manaAsString(player));
        } catch (NullPointerException e) {
            // wrapped in try-catch in-case scoreboard can't set up in time
        }
    }

    private String healthAsString(final Player player) {
        int currentHealth = (int) player.getHealth();
        int maxHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return ChatColor.DARK_RED + "❤ " + ChatColor.RED + currentHealth + " §e/ " + ChatColor.RED + maxHealth + " (Health)";
    }

    private String manaAsString(final Player player) {
        int mana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(player).getMaxMana();
        return ChatColor.DARK_AQUA + "✸ " + mana + " §e/ " + ChatColor.DARK_AQUA + maxMana + " (Mana)";
    }

    private String playerClass(final Player player) {
        String className = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getClassLevel();
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

    private static final String NO_PROF_STRING = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + "None";

    private String playerProf(final Player player) {
        String profName = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfLevel();
        String display;
        if (profName == null) {
            display = NO_PROF_STRING;
        } else {
            display = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + profName;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + profName + ChatColor.YELLOW + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    private static final String NO_GUILD_STRING = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + "None";

    private String playerGuild(final Player player) {
        try {
            String display;
            String guild = RunicCore.getCacheManager().getPlayerCaches().get(player).getGuild();
            if (!guild.equalsIgnoreCase("none")) {
                display = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + guild;
            } else {
                display = NO_GUILD_STRING;
            }
            return display;
        } catch (Exception e) {
            return NO_GUILD_STRING;
        }
    }
}