package us.fortherealm.plugin.outlaw;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import us.fortherealm.plugin.Main;

import java.util.UUID;

// TODO: check if killer/victim have party, perform calculations based on average party rating/pSensitivity instead

public class OutlawEvent implements Listener {

    private RatingCalculator rc = new RatingCalculator();
    private Plugin plugin = Main.getInstance();
    public double getRating(UUID uuid) {
        return plugin.getConfig().getInt(uuid + ".outlaw.rating");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // if the player data cannot be found, use default settings (also sets up new players)
        if (!plugin.getConfig().isSet(uuid + ".outlaw")){
            // TODO: check the outlaw status of a player's guild when setting default values if their data was null, set player's outlaw status to true or false accordingly
            plugin.getConfig().set(uuid + ".outlaw.enabled", false);
            plugin.getConfig().set(uuid + ".outlaw.rating", 1500);
            plugin.saveConfig();
            plugin.reloadConfig();
        }

        // TODO: change this from a scoreboard team to NMS
        Scoreboard s = Main.getInstance().getServer().getScoreboardManager().getMainScoreboard();
        Team outlaw = s.getTeam(player.getName());

        // if a player is an outlaw, add them to their own team and make their nameplate red.
        if (plugin.getConfig().getBoolean(uuid + ".outlaw.enabled", true)) {
            player.sendMessage("you are an outlaw");
            if (outlaw == null) {
                outlaw = s.registerNewTeam(player.getName());
                outlaw.setPrefix(ChatColor.RED + "");
                outlaw.addEntry(player.getName());
            }

        // if a player is not an outlaw, make sure to remove that team and red name.
        } else {
            player.sendMessage("you are not an outlaw");
            if (outlaw.hasEntry(player.getName())) {
                outlaw.unregister();
            }
        }
    }

    //TODO: only make prefix apply in certain channels? / hook into server chat plugin
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // add rating prefix to chat if a player is an outlaw
        if (plugin.getConfig().getBoolean(uuid + ".outlaw.enabled", true)) {
            player.setDisplayName(ChatColor.RED + "[" + (int) getRating(uuid) + "] "
                    + ChatColor.WHITE + player.getName());
        }
    }

    public void onKill(Player damager, Player victim) {

        UUID p1 = damager.getUniqueId();
        UUID p2 = victim.getUniqueId();

        // check that both players are in outlaw mode
        if (plugin.getConfig().getBoolean(p1 + ".outlaw.enabled", true)
                && plugin.getConfig().getBoolean(p2 + ".outlaw.enabled", true)) {
            int r1 = Main.getInstance().getConfig().getInt(p1 + ".outlaw.rating");
            int r2 = Main.getInstance().getConfig().getInt(p2 + ".outlaw.rating");

            // calculate new score for a win "+"
            int newRatingP1 = rc.calculate2PlayersRating(r1, r2, "+", rc.determineK(r1));

            // calculate new score for a loss "-"
            int newRatingP2 = rc.calculate2PlayersRating(r2, r1, "-", rc.determineK(r2));

            // update config values
            Main.getInstance().getConfig().set(p1 + ".outlaw.rating", newRatingP1);
            Main.getInstance().getConfig().set(p2 + ".outlaw.rating", newRatingP2);
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();

            // send players messages and effects
            int changeP1 = newRatingP1 - r1;
            int changeP2 = -(newRatingP2 - r2);
            damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
            sendRatingMessages(damager, victim, changeP1, changeP2);
        }
    }

    private void sendRatingMessages(Player damager, Player victim, int changeP1, int changeP2) {
        damager.sendMessage(ChatColor.DARK_GREEN + "§lYou gained " + ChatColor.GREEN + "§l+" + changeP1 + "§l§2 OS!");
        victim.sendMessage(ChatColor.DARK_RED + "§lYou lost " + ChatColor.RED + "§l-" + changeP2 + "§l§4 OS!");
    }
}
