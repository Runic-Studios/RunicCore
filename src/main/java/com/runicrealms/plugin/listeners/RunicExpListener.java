package com.runicrealms.plugin.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.party.PartyExpPayload;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RunicExpListener implements Listener {

    public static final int LEVEL_CUTOFF = 8;
    public static final double PARTY_BONUS = 0.15;

    /**
     * @param player         player to show hologram to
     * @param location       to spawn the hologram
     * @param linesToDisplay the contents of the hologram
     * @param height         the height up from the original location
     */
    public static void createExpHologram(Player player, Location location, List<String> linesToDisplay, float height) {
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.clone().add(0, height, 0));
        hologram.getVisibilityManager().setVisibleByDefault(false);
        linesToDisplay.forEach(hologram::appendTextLine);
        hologram.getVisibilityManager().showTo(player);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), hologram::delete, 40L); // 2s
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

    /**
     * This code finally dishes out experience once all modifiers have been applied.
     * Also applies logic for party bonus
     */
    @EventHandler(priority = EventPriority.HIGHEST) // executes last
    public void onExperienceGain(RunicExpEvent event) {
        Player player = event.getPlayer();


        // quests and other sources don't get exp modifiers, so skip calculations
        if (event.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST
                || event.getRunicExpSource() == RunicExpEvent.RunicExpSource.OTHER
                || event.getRunicExpSource() == RunicExpEvent.RunicExpSource.PARTY) {
            PlayerLevelUtil.giveExperience(event.getPlayer(), event.getFinalAmount());
            return;
        }

        if (!isInPartyOfMinSizeTwo(player.getUniqueId())) {
            if (event.getLocation() != null) { // world mobs
                Location loc = event.getLocation();
                int plLv = player.getLevel();
                ChatColor expColor = ChatColor.WHITE;
                if (event.getMobLevel() > (plLv + LEVEL_CUTOFF) || event.getMobLevel() < (plLv - LEVEL_CUTOFF)) {
                    event.setFinalAmount(0);
                    expColor = ChatColor.RED;
                }

                createExpHologram(player, loc, Collections.singletonList(ColorUtil.format("&7+ " + expColor + event.getFinalAmount() + " &7exp")), 2.5f);
                createExpHologram(player, loc, Collections.singletonList(ColorUtil.format("&f" + player.getName())), 2.25f);
            }

            PlayerLevelUtil.giveExperience(event.getPlayer(), event.getFinalAmount());

            // Player has valid party
        } else {
            int extraAmt = (int) (event.getOriginalAmount() * PARTY_BONUS);
            if (extraAmt < 1)
                extraAmt = 1;
            event.setFinalAmount(event.getFinalAmount() + extraAmt);
            /*
            Use final amount in this case, so we have Outlaw bonus's included in party bonus.
            (Only outlaws can party with outlaws)
             */
            PartyExpPayload partyExpPayload = new PartyExpPayload
                    (
                            player,
                            RunicCore.getPartyAPI().getParty(player.getUniqueId()),
                            event
                    );
            partyExpPayload.distributePartyExp();
        }
    }
}
