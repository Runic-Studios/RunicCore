package com.runicrealms.plugin.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.commands.admin.BoostCMD;
import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

public class RunicExpListener implements Listener {

    private static final int LEVEL_CUTOFF = 5;
    private static final double PARTY_BONUS = 15;
    private static final int RANGE = 100;

    /**
     * @param player        player to show hologram to
     * @param location      to spawn the hologram
     * @param lineToDisplay the contents of the hologram
     * @param height        the height up from the original location
     */
    private void createExpHologram(Player player, Location location, String lineToDisplay, float height) {
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.clone().add(0, height, 0));
        hologram.getVisibilityManager().setVisibleByDefault(false);
        hologram.appendTextLine(lineToDisplay);
        hologram.getVisibilityManager().showTo(player);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), hologram::delete, 40L); // 2s
    }

    /**
     * @param players a set of players to display the hologram to
     */
    private void createExpHologram(Set<Player> players, Location location, String lineToDisplay, float height) {
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.clone().add(0, height, 0));
        hologram.getVisibilityManager().setVisibleByDefault(false);
        hologram.appendTextLine(lineToDisplay);
        for (Player player : players) {
            hologram.getVisibilityManager().showTo(player);
        }
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), hologram::delete, 40L); // 2s
    }

    /**
     * Method to determine exp distribution for a given party
     *
     * @param party       of player receiving exp
     * @param player      who triggered event
     * @param originalExp of event (before bonuses)
     * @param extraAmt    of party exp
     * @param mobLv       of mob (if applicable)
     * @param location    of mob (if applicable)
     * @param jedis       the jedis resource
     */
    private void distributePartyExp(Party party, Player player, int originalExp, int extraAmt, int mobLv, Location location, Jedis jedis) {

        // determine how many players to split exp among
        int nearbyMembers = 0;
        for (Player member : party.getMembersWithLeader()) {
            if (player.getLocation().getWorld() != member.getLocation().getWorld()) continue;
            if (player.getLocation().distanceSquared(member.getLocation()) < RANGE * RANGE) {
                nearbyMembers += 1;
            }
        }

        for (Player member : party.getMembersWithLeader()) {
            if (player.getLocation().getWorld() != member.getLocation().getWorld()) continue;
            if (player.getLocation().distanceSquared(member.getLocation()) < RANGE * RANGE) {
                int memberLv = member.getLevel();
                if (mobLv > (memberLv + LEVEL_CUTOFF) || mobLv < (memberLv - LEVEL_CUTOFF)) {
                    PlayerLevelUtil.giveExperience(member, 0, jedis);
                    createExpHologram(member, location, ColorUtil.format("&7+ &c0 &7exp"), 2.5f);
                } else {
                    createExpHologram(member, location, ColorUtil.format("&7+ " + ChatColor.WHITE + originalExp + "&a(+" + extraAmt + ") &7exp"), 2.5f);
                    RunicExpEvent e = new RunicExpEvent(originalExp, ((originalExp + extraAmt) / nearbyMembers), member, RunicExpEvent.RunicExpSource.PARTY, mobLv, location);
                    Bukkit.getPluginManager().callEvent(e);
                }
            }
        }


        createExpHologram(party.getMembersWithLeader(), location, ColorUtil.format("&f" + player.getName() + "&7's Party"), 2.25f);
    }

    /**
     * Quick check to see if a player is in a party (and not alone in that party)
     *
     * @param uuid of player to check
     * @return true if the player is in a party of at least 2 members
     */
    private boolean isInPartyOfMinSizeTwo(UUID uuid) {
        return RunicCore.getPartyAPI().hasParty(uuid)
                && RunicCore.getPartyAPI().getParty(uuid).getSize() >= 2;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // executes last
    public void onExperienceGain(RunicExpEvent event) {

        Player player = event.getPlayer();

        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            // quests and other don't get exp modifiers, so skip calculations
            if (event.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST
                    || event.getRunicExpSource() == RunicExpEvent.RunicExpSource.OTHER
                    || event.getRunicExpSource() == RunicExpEvent.RunicExpSource.PARTY) {
                PlayerLevelUtil.giveExperience(event.getPlayer(), event.getFinalAmount(), jedis);
                return;
            }

            // calculate global exp modifier (if applicable)
            double boostPercent = BoostCMD.getCombatExperienceBoost() / 100;
            int boost = (int) boostPercent * event.getOriginalAmount();
            event.setFinalAmount(event.getFinalAmount() + boost);

            if (!isInPartyOfMinSizeTwo(player.getUniqueId())) {
                if (event.getLocation() != null) { // world mobs
                    Location loc = event.getLocation();
                    int plLv = player.getLevel();
                    ChatColor expColor = ChatColor.WHITE;
                    if (event.getMobLevel() > (plLv + LEVEL_CUTOFF) || event.getMobLevel() < (plLv - LEVEL_CUTOFF)) {
                        event.setFinalAmount(0);
                        expColor = ChatColor.RED;
                    }
                    createExpHologram(player, loc, ColorUtil.format("&7+ " + expColor + event.getFinalAmount() + " &7exp"), 2.5f);
                    createExpHologram(player, loc, ColorUtil.format("&f" + player.getName()), 2.25f);
                }
                PlayerLevelUtil.giveExperience(event.getPlayer(), event.getFinalAmount(), jedis);

            } else {
                double partyPercent = PARTY_BONUS / 100;
                int extraAmt = (int) (event.getOriginalAmount() * partyPercent);
                if (extraAmt < 1)
                    extraAmt = 1;
                event.setFinalAmount(event.getFinalAmount() + extraAmt);
                // Use final amount in this case, so we have Outlaw bonus's included in party bonus.
                distributePartyExp
                        (
                                RunicCore.getPartyAPI().getParty(player.getUniqueId()),
                                player,
                                event.getFinalAmount(),
                                extraAmt,
                                event.getMobLevel(),
                                event.getLocation(),
                                jedis
                        );
            }
        }
    }
}
