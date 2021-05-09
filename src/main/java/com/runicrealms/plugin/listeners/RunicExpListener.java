package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.BoostCMD;
import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExperienceGain(RunicExpEvent e) {

        Player pl = e.getPlayer();

        // quests don't get exp modifiers, so skip calculations
        if (e.getRunicExpSource() == RunicExpEvent.RunicExpSource.QUEST) {
            PlayerLevelUtil.giveExperience(e.getPlayer(), e.getAmount());
            return;
        }

        // calculate global exp modifier (if applicable)
        double boostPercent = BoostCMD.getCombatExperienceBoost()/100;
        int boost = (int) boostPercent*e.getAmount();
        e.setAmount(e.getAmount() + boost);

        if (!isInParty(pl)) {
            if (e.getLocation() != null) { // world mobs
                Location loc = e.getLocation();
                int plLv = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassLevel();
                ChatColor expColor = ChatColor.WHITE;
                if (e.getMobLevel() > (plLv + LEVEL_CUTOFF) || e.getMobLevel() < (plLv - LEVEL_CUTOFF)) {
                    e.setAmount(0);
                    expColor = ChatColor.RED;
                }
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ " + expColor + e.getAmount() + " &7exp"), 0, 2.5, 0);
                HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName()), 0, 2.25, 0);
            }
            PlayerLevelUtil.giveExperience(e.getPlayer(), e.getAmount());

        } else {
            double partyPercent = PARTY_BONUS / 100;
            int extraAmt = (int) (e.getAmount() * partyPercent);
            if (extraAmt < 1)
                extraAmt = 1;
            e.setAmount(e.getAmount() + extraAmt);
            distributePartyExp(RunicCore.getPartyManager().getPlayerParty(pl), pl, e.getAmount(), extraAmt, e.getMobLevel(), e.getLocation());
        }
    }

    private boolean isInParty(Player pl) {
        return RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).getSize() >= 2;
    }

    /**
     *
     * @param party of player receiving exp
     * @param pl who triggered event
     * @param exp of event (before bonuses)
     * @param extraAmt of party exp
     * @param mobLv of mob (if applicable)
     * @param loc of mob (if applicable)
     */
    private void distributePartyExp(Party party, Player pl, int exp, int extraAmt, int mobLv, Location loc) {

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
                int memberLv = RunicCore.getCacheManager().getPlayerCaches().get(member).getClassLevel();
                if (mobLv > (memberLv+ LEVEL_CUTOFF) || mobLv < (memberLv- LEVEL_CUTOFF)) {
                    PlayerLevelUtil.giveExperience(member, 0);
                    HologramUtil.createStaticHologram(member, loc.clone(), ColorUtil.format("&7+ &c0 &7exp"), 0, 2.9, 0, true);
                } else {
                    PlayerLevelUtil.giveExperience(member, (exp / nearbyMembers));
                }
            }
        }

        if (loc != null) {
            HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&7+ " + ChatColor.WHITE + exp + "&a(+" + extraAmt + ") &7exp"), 0, 2.6, 0);
            HologramUtil.createStaticHologram(pl, loc.clone(), ColorUtil.format("&f" + pl.getName() + "&7's Party"), 0, 2.3, 0);
        }
    }
}
