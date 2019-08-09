package com.runicrealms.plugin.professions.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.professions.crafting.CookingGUI;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CookingListener implements Listener {

    @EventHandler
    public void onCustomFoodEat(PlayerItemConsumeEvent e) {
        Player pl = e.getPlayer();
        if (e.getItem().equals(CookingGUI.getRabbitStew())) {
            e.setCancelled(true);
            if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId())) {
                pl.sendMessage(ChatColor.RED + "You can't do that in combat!");
                return;
            }
            takeItem(pl, e.getItem());
            pl.setFoodLevel(pl.getFoodLevel() + 6);
            healOverTime(pl, CookingGUI.getRabbitStewAmt(), CookingGUI.getStewDuration(), true);
        } else if (e.getItem().equals(CookingGUI.getAmbrosiaStew())) {
            e.setCancelled(true);
            takeItem(pl, e.getItem());
            pl.setFoodLevel(pl.getFoodLevel() + 6);
            healOverTime(pl, CookingGUI.getAmbrosiaStewAmt(), CookingGUI.getStewDuration(), false);
        }
    }

    /**
     * Removes consumed item from players hand or offhand
     *
     * @param pl   to remove item from
     * @param item from consume event
     */
    private void takeItem(Player pl, ItemStack item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pl.getInventory().getItemInOffHand().equals(item)) {
                    pl.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                } else {
                    pl.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 1L);
    }

    private void healOverTime(Player pl, int healAmt, int duration, boolean isCancelledByCombat) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration) {
                    this.cancel();
                } else {
                    if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId()) && isCancelledByCombat) {
                        this.cancel();
                    }
                    count += 1;
                    HealUtil.healPlayer((healAmt / duration), pl, pl,
                            false, false, false);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }
}
