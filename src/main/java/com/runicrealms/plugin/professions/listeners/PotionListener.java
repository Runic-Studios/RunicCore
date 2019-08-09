package com.runicrealms.plugin.professions.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PotionListener implements Listener {

    // todo: see if a potion cooldown is necessary
    //private static List<UUID> healCDs = new ArrayList<>();
    //private static List<UUID> manaCDs = new ArrayList<>();
    private static List<UUID> slayers = new ArrayList<>();
    private static List<UUID> looters = new ArrayList<>();

    /**
     * Handles custom potions
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPotionUse(PlayerItemConsumeEvent e) {

        if (e.getItem().getType() == Material.POTION) {

            Player pl = e.getPlayer();
            int healAmt = (int) AttributeUtil.getCustomDouble(e.getItem(), "potion.healing");
            int manaAmt = (int) AttributeUtil.getCustomDouble(e.getItem(), "potion.mana");
            int slayingDuration = (int) AttributeUtil.getCustomDouble(e.getItem(), "potion.slaying");
            int lootingDuration = (int) AttributeUtil.getCustomDouble(e.getItem(), "potion.looting");

            // remove glass bottle from inventory, main hand or offhand
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (pl.getInventory().getItemInOffHand().getType() == Material.GLASS_BOTTLE) {
                        pl.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    } else {
                        pl.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }
            }.runTaskLaterAsynchronously(RunicCore.getInstance(), 1L);

            if (healAmt > 0) {
                HealUtil.healPlayer(healAmt, pl, pl, false, false, false);
            }

            if (manaAmt > 0) {
                RunicCore.getManaManager().addMana(pl, manaAmt);
            }

            if (slayingDuration > 0) {
                slayers.add(pl.getUniqueId());
                pl.sendMessage(ColorUtil.format("&eYou've gained a &f20% &edamage bonus vs. monsters for &f" + slayingDuration + " &eminutes!"));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        slayers.remove(pl.getUniqueId());
                    }
                }.runTaskLaterAsynchronously(RunicCore.getInstance(), slayingDuration*60*20L);
            }

            if (lootingDuration > 0) {
                looters.add(pl.getUniqueId());
                pl.sendMessage(ColorUtil.format("&eYou've gained a &f20% &elooting bonus for &f" + lootingDuration + " &eminutes!"));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        looters.remove(pl.getUniqueId());
                    }
                }.runTaskLaterAsynchronously(RunicCore.getInstance(), lootingDuration*60*20L);
            }
        }
    }

    /**
     * Low prio so its calculated last
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMeleeDamage(WeaponDamageEvent e) {

        if (!slayers.contains(e.getPlayer().getUniqueId())) return;

        double percent = 0.2;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) {
            extraAmt = 1;
        }
        e.setAmount(e.getAmount() + extraAmt);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpellDamage(SpellDamageEvent e) {

        if (!slayers.contains(e.getPlayer().getUniqueId())) return;

        double percent = 0.2;
        int extraAmt = (int) (e.getAmount() * percent);
        if (extraAmt < 1) {
            extraAmt = 1;
        }
        e.setAmount(e.getAmount() + extraAmt);
    }
}
