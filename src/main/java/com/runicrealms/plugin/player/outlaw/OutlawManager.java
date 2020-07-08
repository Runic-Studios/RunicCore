package com.runicrealms.plugin.player.outlaw;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class OutlawManager {

    private final int BASE_RATING = 1500;
    private final RatingCalculator rc = new RatingCalculator();

    public static boolean isOutlaw(Player pl) {
        return RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getIsOutlaw();
    }

    /**
     *
     * @param damager
     * @param victim
     */
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
        r1 = getR1(p1, p1Party, r1);
        r2 = getR1(p2, p2Party, r2);

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

    /**
     *
     * @param p1
     * @param p1Party
     * @param r1
     * @return
     */
    private int getR1(UUID p1, Party p1Party, int r1) {
        if (p1Party != null) {
            for (Player partyMember : p1Party.getMembers()) {
                r1 += RunicCore.getCacheManager().getPlayerCache(partyMember.getUniqueId()).getRating();
            }
            r1 = r1 / (p1Party.getSize());
        } else {
            r1 = RunicCore.getCacheManager().getPlayerCache(p1).getRating();
        }
        return r1;
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
