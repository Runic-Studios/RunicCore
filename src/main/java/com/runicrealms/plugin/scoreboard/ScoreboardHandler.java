package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

        // sets players username red if they are outlaw.
        new BukkitRunnable() {
            @Override
            public void run() {
                updateSideInfo(pl);
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 20L);
    }

    private void createScoreboard(Player pl){

        // create our scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        // create teams to be used for name colors later
        Team white = board.registerNewTeam("white");
        white.setColor(ChatColor.WHITE);
        Team party = board.registerNewTeam("party");
        party.setColor(ChatColor.GREEN);
        party.setCanSeeFriendlyInvisibles(true);
        Team outlaw = board.registerNewTeam("outlaw");
        outlaw.setColor(ChatColor.RED);
        Team npc = board.registerNewTeam("npc");
        npc.setColor(ChatColor.WHITE);
        npc.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        // setup side scoreboard
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(ChatColor.LIGHT_PURPLE + "     §lRunic Realms     ");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        // set the board!
        pl.setScoreboard(board);
    }

    private static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        return Class.forName(name);
    }

    private static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle");
        Object nmsPlayer = getHandle.invoke(player);
        Field conField = nmsPlayer.getClass().getField("playerConnection");
        return conField.get(nmsPlayer);
    }

    /**
     * Updates player name colors using packets and reflection
     * @param player who will receive packets
     * @param team name of team, "party" for green and "outlaw" for red
     * @param whoseNames player(s) whose names are "looked at"
     */
    public static void updateNamesFor(Player player, Team team, List<String> whoseNames)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Class<?> nmsScoreboard = getNMSClass("Scoreboard");
        Constructor<?> nmsScoreboardConstructor = nmsScoreboard.getConstructor();
        Object scoreboardObj = nmsScoreboardConstructor.newInstance();
        Class<?> nmsTeam = getNMSClass("ScoreboardTeam"); //new ScoreboardTeam(nmsScoreboard, team.getName());
        Constructor<?> nmsTeamConstructor = nmsTeam.getConstructor(getNMSClass("Scoreboard"), String.class);
        Object nmsTeamObj = nmsTeamConstructor.newInstance(scoreboardObj, team.getName());
        Class<?> packetClass = getNMSClass("PacketPlayOutScoreboardTeam");
        Constructor<?> packetConstructor = packetClass.getConstructor(nmsTeam, Collection.class, int.class);
        Object packet = packetConstructor.newInstance(nmsTeamObj, whoseNames, 3);
        Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(getConnection(player), packet);
    }

    private void updateSideInfo(Player pl){

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
        Score characterInfo = sidebar.getScore(ChatColor.YELLOW + "" + ChatColor.BOLD + "Character");
        characterInfo.setScore(7);

        updatePlayerInfo(pl);

        // setup side health display
        Score health = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(healthAsString(pl));
        health.setScore(2);
        Score mana = pl.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getScore(manaAsString(pl));
        mana.setScore(1);
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
        int mana = RunicCore.getManaManager().getCurrentManaList().get(pl.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getMaxMana();
        return ChatColor.DARK_AQUA + "✸ " + mana + " §e/ " + ChatColor.DARK_AQUA + maxMana + " (Mana)";
    }

    private String playerClass(Player pl) {
        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel();
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
        String profName = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getProfName();
        int currentLevel = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getProfLevel();
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
            String guild = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getGuild();
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