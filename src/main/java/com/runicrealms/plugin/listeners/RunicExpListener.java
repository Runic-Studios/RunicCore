package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.commands.admin.BoostCMD;
import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RunicExpListener implements Listener {

    private static final int LEVEL_CUTOFF = 5;
    private static final double PARTY_BONUS = 15;
    private static final int RANGE = 100;

    @EventHandler(priority = EventPriority.HIGHEST) // executes last
    public void onExperienceGain(RunicExpEvent e) {

        Player player = e.getPlayer();

        // quests and other don't get exp modifiers, so skip calculations
        if (e.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST
                || e.getRunicExpSource() == RunicExpEvent.RunicExpSource.OTHER
                || e.getRunicExpSource() == RunicExpEvent.RunicExpSource.PARTY) {
            PlayerLevelUtil.giveExperience(e.getPlayer(), e.getFinalAmount());
            return;
        }

        // calculate global exp modifier (if applicable)
        double boostPercent = BoostCMD.getCombatExperienceBoost() / 100;
        int boost = (int) boostPercent * e.getOriginalAmount();
        e.setFinalAmount(e.getFinalAmount() + boost);

        if (!isInParty(player)) {
            if (e.getLocation() != null) { // world mobs
                Location loc = e.getLocation();
                int plLv = player.getLevel();
                ChatColor expColor = ChatColor.WHITE;
                if (e.getMobLevel() > (plLv + LEVEL_CUTOFF) || e.getMobLevel() < (plLv - LEVEL_CUTOFF)) {
                    e.setFinalAmount(0);
                    expColor = ChatColor.RED;
                }
                HologramUtil.createStaticHologram(player, loc.clone(), ColorUtil.format("&7+ " + expColor + e.getFinalAmount() + " &7exp"), 0, 2.5, 0);
                HologramUtil.createStaticHologram(player, loc.clone(), ColorUtil.format("&f" + player.getName()), 0, 2.25, 0);
            }
            PlayerLevelUtil.giveExperience(e.getPlayer(), e.getFinalAmount());

        } else {
            double partyPercent = PARTY_BONUS / 100;
            int extraAmt = (int) (e.getOriginalAmount() * partyPercent);
            if (extraAmt < 1)
                extraAmt = 1;
            e.setFinalAmount(e.getFinalAmount() + extraAmt);
            //Use final amount in this case so we have Outlaw bonus's included in party bonus.
            distributePartyExp(RunicCore.getPartyManager().getPlayerParty(player), player, e.getFinalAmount(), extraAmt, e.getMobLevel(), e.getLocation());
        }
    }

    private boolean isInParty(Player pl) {
        return RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).getSize() >= 2;
    }

    /**
     * @param party       of player receiving exp
     * @param pl          who triggered event
     * @param originalExp of event (before bonuses)
     * @param extraAmt    of party exp
     * @param mobLv       of mob (if applicable)
     * @param loc         of mob (if applicable)
     */
    private void distributePartyExp(Party party, Player pl, int originalExp, int extraAmt, int mobLv, Location loc) {

        // determine how many players to split exp among
        int nearbyMembers = 0;
        for (Player member : party.getMembersWithLeader()) {
            if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;
            if (pl.getLocation().distance(member.getLocation()) < RANGE) {
                nearbyMembers += 1;
            }
        }

        for (Player member : party.getMembersWithLeader()) {
            if (pl.getLocation().getWorld() != member.getLocation().getWorld()) continue;
            if (pl.getLocation().distance(member.getLocation()) < RANGE) {
                int memberLv = member.getLevel();
                if (mobLv > (memberLv + LEVEL_CUTOFF) || mobLv < (memberLv - LEVEL_CUTOFF)) {
                    PlayerLevelUtil.giveExperience(member, 0);
                    HologramUtil.createStaticHologram(member, loc.clone(), ColorUtil.format("&7+ &c0 &7exp"), 0, 2.9, 0, true);
                } else {
                    RunicExpEvent e = new RunicExpEvent(originalExp, ((originalExp + extraAmt) / nearbyMembers), member, RunicExpEvent.RunicExpSource.PARTY, mobLv, loc);
                    Bukkit.getPluginManager().callEvent(e);
                }
            }
        }

        if (loc != null) {
            HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ " + ChatColor.WHITE + originalExp + "&a(+" + extraAmt + ") &7exp"), 0, 2.6, 0);
            HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName() + "&7's Party"), 0, 2.3, 0);
        }
    }
}
