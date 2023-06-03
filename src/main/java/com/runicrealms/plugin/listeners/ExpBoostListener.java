package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.donor.boost.api.BoostExperienceType;
import com.runicrealms.plugin.events.RunicExpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Applies an additive global experience boost to experience gains from monsters
 */
public class ExpBoostListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExpGain(RunicExpEvent event) {
        if (event.isCancelled()) return;
        if (event.getRunicExpSource() != RunicExpEvent.RunicExpSource.MOB) return; // only mobs
        double booster = RunicCore.getBoostAPI().getAdditionalExperienceMultiplier(BoostExperienceType.COMBAT);
        int boosted = (int) booster * event.getOriginalAmount();
        event.setFinalAmount(event.getFinalAmount() + boosted);
    }
}
