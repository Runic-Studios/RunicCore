package us.fortherealm.plugin.outlaw;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.parties.Party;

import java.util.UUID;

public class OutlawManager implements Listener {

    private RatingCalculator rc = new RatingCalculator();
    private Plugin plugin = FTRCore.getInstance();
    public double getRating(UUID uuid) {
        return plugin.getConfig().getInt(uuid + ".info.rating");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // if the player data cannot be found, use default settings (also sets up new players)
        if (!plugin.getConfig().isSet(uuid + ".info.rating")){
            plugin.getConfig().set(uuid + ".info.rating", 1500);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
    }

    // uses the rating calculator to apply ratings between two outlaws
    public void onKill(Player damager, Player victim) {

        UUID p1 = damager.getUniqueId();
        UUID p2 = victim.getUniqueId();
        Party p1Party = FTRCore.getPartyManager().getPlayerParty(damager);
        Party p2Party = FTRCore.getPartyManager().getPlayerParty(victim);
        int r1 = 0;
        int r2 = 0;

        // todo: check for the guild settings, NOT per-player before applying rating mechanics
        //sef9esfesfe7sfesf79es8f7es---sefesfes
            //name: idiots
            //outlaw: false
        // todo: add proportion calculator
        // if (p1.guild.outaw.isEnabled...)
            // if the player has a party, calculate that party's average rating
            // otherwise, the r1 is simply the player's current rating
            if (p1Party != null) {
                for (UUID partyMember : p1Party.getMembers()) {
                    r1 += FTRCore.getInstance().getConfig().getInt(partyMember + ".info.rating");
                }
                r1 = r1/(p1Party.getPartySize());
            } else {
                r1 = FTRCore.getInstance().getConfig().getInt(p1 + ".info.rating");
            }
            if (p2Party != null) {
                for (UUID partyMember : p2Party.getMembers()) {
                    r2 += FTRCore.getInstance().getConfig().getInt(partyMember + ".info.rating");
                }
                r2 = r2/(p2Party.getPartySize());
            } else {
                r2 = FTRCore.getInstance().getConfig().getInt(p2 + ".info.rating");
            }

            // calculate new score for a win "+"
            int newRatingP1 = rc.calculateRating(r1, r2, "+", rc.determineK(r1));

            // calculate new score for a loss "-"
            int newRatingP2 = rc.calculateRating(r2, r1, "-", rc.determineK(r2));

            // update config values
            FTRCore.getInstance().getConfig().set(p1 + ".info.rating", newRatingP1);
            FTRCore.getInstance().getConfig().set(p2 + ".info.rating", newRatingP2);
            FTRCore.getInstance().saveConfig();
            FTRCore.getInstance().reloadConfig();

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
