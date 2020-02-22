package com.runicrealms.plugin.player.outlaw;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.parties.Party;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class OutlawManager implements Listener {

    private final int BASE_RATING = 1500;
    private RatingCalculator rc = new RatingCalculator();
    private Plugin plugin = RunicCore.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharacterLoad(CharacterLoadEvent e) {

        Player pl = e.getPlayer();

        // sets players username red for OTHER players if they are outlaw, delayed by 2s to ensure scoreboard team exists
        new BukkitRunnable() {
            @Override
            public void run() {
                if (RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getIsOutlaw()) {
                    for (Player on : RunicCore.getCacheManager().getLoadedPlayers()) {
                        try {
                        ScoreboardHandler.updateNamesFor(on, pl.getScoreboard().getTeam("outlaw"),
                                Collections.singletonList(pl.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (Player on : RunicCore.getCacheManager().getLoadedPlayers()) {
                        try {
                        ScoreboardHandler.updateNamesFor(on, pl.getScoreboard().getTeam("white"),
                                Collections.singletonList(pl.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // updates OTHER players names for joined user
                for (Player on : RunicCore.getCacheManager().getLoadedPlayers()) {
                    if (RunicCore.getCacheManager().getPlayerCache(on.getUniqueId()).getIsOutlaw()) {
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
        return RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getIsOutlaw();
    }

    // uses the rating calculator to apply ratings between two outlaws
    public void onKill(Player damager, Player victim) {

        UUID p1 = damager.getUniqueId();
        UUID p2 = victim.getUniqueId();
        Party p1Party = RunicCore.getPartyManager().getPlayerParty(damager);
        Party p2Party = RunicCore.getPartyManager().getPlayerParty(victim);
        int r1 = 0;
        int r2 = 0;

        // todo: add proportion calculator?
        // if the player has a party, calculate that party's average rating
        // otherwise, the r1 is simply the player's current rating
        if (p1Party != null) {
            for (UUID partyMember : p1Party.getMembers()) {
                r1 += RunicCore.getCacheManager().getPlayerCache(partyMember).getRating();
            }
            r1 = r1 / (p1Party.getPartySize());
        } else {
            r1 = RunicCore.getCacheManager().getPlayerCache(p1).getRating();
        }
        if (p2Party != null) {
            for (UUID partyMember : p2Party.getMembers()) {
                r2 += RunicCore.getCacheManager().getPlayerCache(partyMember).getRating();
            }
            r2 = r2 / (p2Party.getPartySize());
        } else {
            r2 = RunicCore.getCacheManager().getPlayerCache(p2).getRating();
        }

        // calculate new score for a win "+"
        int newRatingP1 = rc.calculateRating(r1, r2, "+", rc.determineK(r1));

        // calculate new score for a loss "-"
        int newRatingP2 = rc.calculateRating(r2, r1, "-", rc.determineK(r2));

        // update rating values
        RunicCore.getCacheManager().getPlayerCache(p1).setRating(newRatingP1);
        RunicCore.getCacheManager().getPlayerCache(p2).setRating(newRatingP2);

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

    public int getBaseRating() {
        return BASE_RATING;
    }
}
