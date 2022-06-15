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
import org.bukkit.scoreboard.*;

public class ScoreboardHandler implements Listener {

    private static final String HEALTH_TEAM_STRING = "health";
    private static final String HEALTH_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.RED;
    private static final String MANA_TEAM_STRING = "MANA";
    private static final String MANA_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.AQUA;

    public ScoreboardHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
                updatePlayerCombatInfo(player, player.getScoreboard());
            }
        }, 100L, 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerJoin(CharacterLoadEvent e) {
        Player player = e.getPlayer();
        setupScoreboard(player);
        NametagUtil.updateNametag(player);
    }

    /**
     * Set the scoreboard for the given player if they do not yet have one
     *
     * @param player to receive scoreboard
     */
    public void setupScoreboard(final Player player) {
        assert Bukkit.getScoreboardManager() != null;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("ServerName", "", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "     Runic Realms     ");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        // setup basic fields
        Score blankSpaceSeven = obj.getScore("§1");
        blankSpaceSeven.setScore(8);
        Score characterInfo = obj.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName());
        characterInfo.setScore(7);
        setupPlayerInfo(player, obj);
        Score blankSpaceTwo = obj.getScore("§2");
        blankSpaceTwo.setScore(3);
        // setup combat fields using teams to avoid flickering
        setupPlayerCombatInfo(player, scoreboard, obj);
        updatePlayerCombatInfo(player, scoreboard);
        player.setScoreboard(scoreboard);
    }

    /**
     * Initial setup for basic scoreboard fields
     *
     * @param player
     * @param objective
     */
    private void setupPlayerInfo(final Player player, final Objective objective) {
        Score playerClass = objective.getScore(playerClass(player));
        playerClass.setScore(6);
        Score playerProfession = objective.getScore(playerProf(player));
        playerProfession.setScore(5);
        Score playerGuild = objective.getScore(playerGuild(player));
        playerGuild.setScore(4);
    }

    /**
     * Initial setup for player combat info using scoreboard teams to prevent flickering
     *
     * @param player
     * @param scoreboard
     * @param obj
     */
    private void setupPlayerCombatInfo(final Player player, final Scoreboard scoreboard, final Objective obj) {
        Team playerHealth = scoreboard.registerNewTeam(player.getName() + HEALTH_TEAM_STRING);
        playerHealth.addEntry(HEALTH_ENTRY_STRING);
        obj.getScore(HEALTH_ENTRY_STRING).setScore(2);
        Team playerMana = scoreboard.registerNewTeam(player.getName() + MANA_TEAM_STRING);
        playerMana.addEntry(MANA_ENTRY_STRING);
        obj.getScore(MANA_ENTRY_STRING).setScore(1);
    }

    /**
     * Method used to keep scoreboard accurate on an async timer
     *
     * @param player
     * @param scoreboard
     */
    private void updatePlayerCombatInfo(final Player player, final Scoreboard scoreboard) {
        try {
            Team playerHealth = scoreboard.getTeam(player.getName() + HEALTH_TEAM_STRING);
            assert playerHealth != null;
            playerHealth.setPrefix(healthAsString(player));
            Team playerMana = scoreboard.getTeam(player.getName() + MANA_TEAM_STRING);
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

    private String playerProf(final Player player) {
        String profName = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCaches().get(player).getProfLevel();
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

    private String playerGuild(final Player player) {
        try {
            String display;
            String guild = RunicCore.getCacheManager().getPlayerCaches().get(player).getGuild();
            if (!guild.equalsIgnoreCase("none")) {
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