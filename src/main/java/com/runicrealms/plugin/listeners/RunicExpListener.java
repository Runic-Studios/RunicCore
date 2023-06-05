package com.runicrealms.plugin.listeners;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.events.RunicCombatExpEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.LinkedList;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExperienceGainParty(RunicCombatExpEvent event) {
        if (isInPartyOfMinSizeTwo(event.getPlayer().getUniqueId())) {
            event.setBonus(RunicCombatExpEvent.BonusType.PARTY, PARTY_BONUS);
        }
    }

    /**
     * This code finally dishes out experience once all modifiers have been applied.
     * Also applies logic for party bonus
     */
    @EventHandler(priority = EventPriority.HIGHEST) // executes last
    public void onExperienceGain(RunicCombatExpEvent event) {
        Player player = event.getPlayer();
        if (event.getHologramLocation() != null) { // world mobs
            List<String> hologramList = new LinkedList<>();
            hologramList.add(ColorUtil.format("&7+ &f" + event.getAmountNoBonuses() + " &7exp"));
            int boostExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.BOOST);
            if (boostExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + boostExpBonus + " &7boost exp"));
            int partyExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.PARTY);
            if (partyExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + partyExpBonus + " &7party exp"));
            int wisdomExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.WISDOM);
            if (wisdomExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + wisdomExpBonus + " &7wisdom exp"));
            int guildExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.GUILD);
            if (guildExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + guildExpBonus + " &7guild exp"));
            int voteExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.VOTE);
            if (voteExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + voteExpBonus + " &7vote exp"));
            int outlawExpBonus = event.getExpFromBonus(RunicCombatExpEvent.BonusType.OUTLAW);
            if (outlawExpBonus != 0) hologramList.add(ColorUtil.format("&7+ &d" + outlawExpBonus + " &7outlaw exp"));
            hologramList.add("&f" + player.getName());
            createExpHologram(player, event.getHologramLocation(), hologramList, 2.5f);
        }

        PlayerLevelUtil.giveExperience(event.getPlayer(), event.getFinalAmount());
    }
}
