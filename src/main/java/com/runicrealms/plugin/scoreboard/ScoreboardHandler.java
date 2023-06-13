package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.ScoreboardAPI;
import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHandler implements ScoreboardAPI {

    // basic info
    private static final String CLASS_TEAM_STRING = "c";
    private static final String CLASS_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.DARK_GREEN;
    private static final String PROF_TEAM_STRING = "p";
    private static final String PROF_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.GREEN;
    private static final String GUILD_TEAM_STRING = "g";
    private static final String GUILD_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.GOLD;
    private static final String OUTLAW_TEAM_STRING = "o";
    private static final String OUTLAW_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.DARK_RED;
    // combat team info
    private static final String HEALTH_TEAM_STRING = "h";
    private static final String HEALTH_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.RED;
    private static final String MANA_TEAM_STRING = "m";
    private static final String MANA_ENTRY_STRING = ChatColor.BLACK + "" + ChatColor.AQUA;
    private static final String NO_CLASS_STRING = ChatColor.YELLOW + "Class: " + ChatColor.GREEN + "None";
    private static final String NO_PROF_STRING = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + "None";
    private static final String NO_GUILD_STRING = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + "None";
    private static final String OUTLAW_DISABLED_STRING = ChatColor.YELLOW + "Outlaw: " + ChatColor.GREEN + "OFF";
    private final Map<UUID, Short> playerIndexes = new HashMap<>();
    private short PLAYER_INDEX = 0;

    /**
     * Create running task to update health / mana display
     */
    public ScoreboardHandler() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                updatePlayerCombatInfo(player, player.getScoreboard());
            }
        }, 100L, 4L);
    }

    private short getNextPlayerIndex() {
        return PLAYER_INDEX++;
    }

    private String getPlayerIndex(UUID player) {
        Short index = playerIndexes.get(player);
        if (index == null) {
            index = getNextPlayerIndex();
            playerIndexes.put(player, index);
        }
        return index.toString();
    }

    private String healthAsString(final Player player) {
        int currentHealth = (int) player.getHealth();
        int maxHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        return ChatColor.DARK_RED + "❤ " + ChatColor.RED + currentHealth + " §e/ " + ChatColor.RED + maxHealth + " (Health)";
    }

    private String manaAsString(final Player player) {
        int mana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        int maxMana = ManaListener.calculateMaxMana(player);
        return ChatColor.DARK_AQUA + "✸ " + mana + " §e/ " + ChatColor.DARK_AQUA + maxMana + " (Mana)";
    }

    private String playerClass(final Player player) {
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        int currentLevel = player.getLevel();
        String display;
        if (className == null) {
            display = NO_CLASS_STRING;
        } else {
            display = ChatColor.YELLOW + "Class: " + ChatColor.GREEN + className;
            if (currentLevel != 0) {
                display = ChatColor.GREEN + className + ChatColor.YELLOW + " lv. " + ChatColor.GREEN + currentLevel;
            }
        }
        return display;
    }

    /**
     * Update the scoreboard info on the player's current guild data
     *
     * @param guildName of the guild
     * @return a string with their guild info
     */
    private String playerGuild(final String guildName) {
        String display;
        if (guildName.equalsIgnoreCase("")) {
            display = NO_GUILD_STRING;
        } else {
            display = ChatColor.YELLOW + "Guild: " + ChatColor.GREEN + guildName;
        }
        return display;
    }

    /**
     * Update the scoreboard info on the player's current outlaw status
     *
     * @param isOutlaw whether the player is an outlaw
     * @return a formatted string to display their outlaw status
     */
    private String playerOutlaw(boolean isOutlaw) {
        String display;
        if (!isOutlaw) {
            display = OUTLAW_DISABLED_STRING;
        } else {
            display = ChatColor.YELLOW + "Outlaw: " + ChatColor.RED + "ON";
        }
        return display;
    }

    /**
     * Update the scoreboard info on the player's current profession data
     *
     * @param profession of the player
     * @param level      of the player's profession
     */
    private String playerProf(final String profession, final int level) {
        String display;
        if (profession.equalsIgnoreCase("")) {
            display = NO_PROF_STRING;
        } else {
            display = ChatColor.YELLOW + "Prof: " + ChatColor.GREEN + profession;
            if (level != 0) {
                display = ChatColor.GREEN + profession + ChatColor.YELLOW + " lv. " + ChatColor.GREEN + level;
            }
        }
        return display;
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
        String index = getPlayerIndex(player.getUniqueId());
        Team playerHealth = scoreboard.registerNewTeam(index + HEALTH_TEAM_STRING);
        playerHealth.addEntry(HEALTH_ENTRY_STRING);
        obj.getScore(HEALTH_ENTRY_STRING).setScore(2);
        Team playerMana = scoreboard.registerNewTeam(index + MANA_TEAM_STRING);
        playerMana.addEntry(MANA_ENTRY_STRING);
        obj.getScore(MANA_ENTRY_STRING).setScore(1);
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
        String index = getPlayerIndex(player.getUniqueId());

        Team playerClass = scoreboard.registerNewTeam(index + CLASS_TEAM_STRING);
        playerClass.addEntry(CLASS_ENTRY_STRING);
        obj.getScore(CLASS_ENTRY_STRING).setScore(7);
        playerClass.setPrefix(playerClass(player)); // setup class prefix ONCE
        Team playerProf = scoreboard.registerNewTeam(index + PROF_TEAM_STRING);
        playerProf.addEntry(PROF_ENTRY_STRING);
        obj.getScore(PROF_ENTRY_STRING).setScore(6);
        Team playerGuild = scoreboard.registerNewTeam(index + GUILD_TEAM_STRING);
        playerGuild.addEntry(GUILD_ENTRY_STRING);
        obj.getScore(GUILD_ENTRY_STRING).setScore(5);
        Team playerOutlaw = scoreboard.registerNewTeam(index + OUTLAW_TEAM_STRING);
        playerOutlaw.addEntry(OUTLAW_ENTRY_STRING);
        obj.getScore(OUTLAW_ENTRY_STRING).setScore(4);
    }

    @Override
    public void setupScoreboard(final Player player) {
        assert Bukkit.getScoreboardManager() != null;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("ServerName", "", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "     Runic Realms     ");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        // setup basic fields
        Score blankSpaceSeven = obj.getScore("§1");
        blankSpaceSeven.setScore(9);
        Score characterInfo = obj.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName());
        characterInfo.setScore(8);
        setupPlayerInfo(player, scoreboard, obj);
        // updatePlayerInfo(player, scoreboard);
        Score blankSpaceTwo = obj.getScore("§2");
        blankSpaceTwo.setScore(3);
        // setup combat fields using teams to avoid flickering
        setupPlayerCombatInfo(player, scoreboard, obj);
        updatePlayerCombatInfo(player, scoreboard);
        player.setScoreboard(scoreboard);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(new ScoreboardUpdateEvent(player, scoreboard)), 1L);
    }

    /**
     * Method used to keep scoreboard accurate during level-up, profession change, etc.
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player who owns the scoreboard
     * @param event  the scoreboard update event that was triggered async
     */
    @Override
    public void updatePlayerInfo(final Player player, final ScoreboardUpdateEvent event) {
        Scoreboard scoreboard = event.getScoreboard();
        String index = getPlayerIndex(player.getUniqueId());

        Team playerClass = scoreboard.getTeam(index + CLASS_TEAM_STRING);
        if (playerClass != null) playerClass.setPrefix(playerClass(player));

        Team playerProf = scoreboard.getTeam(index + PROF_TEAM_STRING);
        if (playerProf != null) playerProf.setPrefix(playerProf(event.getProfession(), event.getProfessionLevel()));
        Team playerGuild = scoreboard.getTeam(index + GUILD_TEAM_STRING);
        if (playerGuild != null) playerGuild.setPrefix(playerGuild(event.getGuild()));
        assert playerGuild != null;
        Team playerOutlaw = scoreboard.getTeam(index + OUTLAW_TEAM_STRING);
        if (playerOutlaw != null) playerOutlaw.setPrefix(playerOutlaw(event.isOutlaw()));
    }

    @Override
    public void updatePlayerScoreboard(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            if (!RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().contains(player.getUniqueId())) return;
            Bukkit.getPluginManager().callEvent(new ScoreboardUpdateEvent(player, player.getScoreboard()));
        });
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
            String index = getPlayerIndex(player.getUniqueId());
            Team playerHealth = scoreboard.getTeam(index + HEALTH_TEAM_STRING);
            assert playerHealth != null;
            playerHealth.setPrefix(healthAsString(player));
            Team playerMana = scoreboard.getTeam(index + MANA_TEAM_STRING);
            assert playerMana != null;
            playerMana.setPrefix(manaAsString(player));
        } catch (NullPointerException e) {
            // wrapped in try-catch in-case scoreboard can't set up in time
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerIndexes.remove(event.getPlayer().getUniqueId());
    }
}