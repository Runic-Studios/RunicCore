package com.runicrealms.plugin.outlaw;

import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.UUID;

public class OutlawManager implements Listener {

    private RatingCalculator rc = new RatingCalculator();
    private Plugin plugin = RunicCore.getInstance();
    public double getRating(UUID uuid) {
        return plugin.getConfig().getInt(uuid + ".info.outlaw.rating");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        // if the player data cannot be found, use default settings (also sets up new players)
        if (!plugin.getConfig().isSet(uuid + ".info.outlaw.rating")){
            plugin.getConfig().set(uuid + ".info.outlaw.rating", 1500);
            plugin.saveConfig();
            plugin.reloadConfig();
        }

        // by default, players are NOT outlaws
        if (!plugin.getConfig().isSet(uuid + ".info.outlaw.enabled")) {
            plugin.getConfig().set(uuid + ".info.outlaw.enabled", false);
            plugin.saveConfig();
            plugin.reloadConfig();
        }

        // sets players username red for OTHER players if they are outlaw, delayed by 2s to ensure scoreboard team exists
        new BukkitRunnable() {
            @Override
            public void run() {
                if (RunicCore.getInstance().getConfig().getBoolean(pl.getUniqueId() + ".info.outlaw.enabled")) {
                    for (Player on : Bukkit.getOnlinePlayers()) {
                        try {
                        ScoreboardHandler.updateNamesFor(on, pl.getScoreboard().getTeam("outlaw"),
                                Collections.singletonList(pl.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (Player on : Bukkit.getOnlinePlayers()) {
                        try {
                        ScoreboardHandler.updateNamesFor(on, pl.getScoreboard().getTeam("white"),
                                Collections.singletonList(pl.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // updates OTHER players names for joined user
                for (Player on : Bukkit.getOnlinePlayers()) {
                    if (RunicCore.getInstance().getConfig().getBoolean(on.getUniqueId() + ".info.outlaw.enabled")) {
                        try {
                        ScoreboardHandler.updateNamesFor(pl, pl.getScoreboard().getTeam("outlaw"),
                                Collections.singletonList(on.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 40L);
    }

    public static boolean isOutlaw(Player pl) {
        return RunicCore.getInstance().getConfig().getBoolean(pl.getUniqueId() + ".info.outlaw.enabled");
    }

    // uses the rating calculator to apply ratings between two outlaws
    public void onKill(Player damager, Player victim) {

        UUID p1 = damager.getUniqueId();
        UUID p2 = victim.getUniqueId();
        Party p1Party = RunicCore.getPartyManager().getPlayerParty(damager);
        Party p2Party = RunicCore.getPartyManager().getPlayerParty(victim);
        int r1 = 0;
        int r2 = 0;

        // todo: add proportion calculator
        // if the player has a party, calculate that party's average rating
        // otherwise, the r1 is simply the player's current rating
        if (p1Party != null) {
            for (UUID partyMember : p1Party.getMembers()) {
                r1 += RunicCore.getInstance().getConfig().getInt(partyMember + ".info.outlaw.rating");
            }
            r1 = r1 / (p1Party.getPartySize());
        } else {
            r1 = RunicCore.getInstance().getConfig().getInt(p1 + ".info.outlaw.rating");
        }
        if (p2Party != null) {
            for (UUID partyMember : p2Party.getMembers()) {
                r2 += RunicCore.getInstance().getConfig().getInt(partyMember + ".info.outlaw.rating");
            }
            r2 = r2 / (p2Party.getPartySize());
        } else {
            r2 = RunicCore.getInstance().getConfig().getInt(p2 + ".info.outlaw.rating");
        }

        // calculate new score for a win "+"
        int newRatingP1 = rc.calculateRating(r1, r2, "+", rc.determineK(r1));

        // calculate new score for a loss "-"
        int newRatingP2 = rc.calculateRating(r2, r1, "-", rc.determineK(r2));

        // update config values
        RunicCore.getInstance().getConfig().set(p1 + ".info.outlaw.rating", newRatingP1);
        RunicCore.getInstance().getConfig().set(p2 + ".info.outlaw.rating", newRatingP2);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();

        // send players messages and effects
        int changeP1 = newRatingP1 - r1;
        int changeP2 = -(newRatingP2 - r2);
        damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        sendRatingMessages(damager, victim, changeP1, changeP2);
    }

    private void sendRatingMessages(Player damager, Player victim, int changeP1, int changeP2) {

        damager.sendTitle("", ChatColor.DARK_GREEN + "You gained "
                + ChatColor.GREEN + changeP1
                + ChatColor.DARK_GREEN + " rating!", 10, 40, 10);

        victim.sendTitle("", ChatColor.DARK_RED + "You lost "
                + ChatColor.RED + changeP2
                + ChatColor.DARK_RED + " rating!", 10, 40, 10);
    }
}
