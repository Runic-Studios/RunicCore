package com.runicrealms.plugin.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * Disables vanilla experience drops.
 * Also disables players from gaining experience at max level.
 */
public class ExpListener implements Listener {

    @EventHandler
    public void onExpDrop(EntityDeathEvent e) {
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onExpGain(PlayerExpChangeEvent e) {
        if (e.getPlayer().getLevel() >= 50) {
            e.setAmount(0);

            // .setExp(0.1) --> takes a percentage, 0 to 1 being 'next level.'
            // so, jus decide on a curve for now. make the experience bar match the percent towards next level in the curve,
            // and manually add the levelup.
            // SAVE EXP TO CONFIG ON LOGOUT
        }
    }
}
