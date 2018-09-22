package us.fortherealm.plugin.listeners;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class ScoreboardHealthListener implements Listener {

    ScoreboardUtil boardUtil = new ScoreboardUtil();
    private Main plugin = Main.getInstance();

    @EventHandler
    public void onDamage (EntityDamageEvent e) {

        LivingEntity victim = (LivingEntity) e.getEntity();

        if (victim instanceof Player) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    boardUtil.updateSideScoreboard((Player) victim);//does what you think it does
                    boardUtil.updateHealthBar((Player) victim);//updates enemy health bars below their name
                }
            }, 1);//1 tick(s)
        }
    }

    @EventHandler
    public void onRegen (EntityRegainHealthEvent e) {

        LivingEntity regenEntity = (LivingEntity) e.getEntity();

        if (regenEntity instanceof Player) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    boardUtil.updateSideScoreboard((Player) regenEntity);
                    boardUtil.updateHealthBar((Player) regenEntity);
                }
            }, 1);//1 tick(s)
        }
    }

    @EventHandler
    public void onArmorEquip (ArmorEquipEvent e) {

        Player playerWhoEquipped = e.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                boardUtil.updateSideScoreboard((Player) playerWhoEquipped);
                boardUtil.updateHealthBar((Player) playerWhoEquipped);
            }
        }, 20);//2 tick(s) = 1.0s so it runs after the hp change from HealthScaleListener in the main plugin

        if (e.getNewArmorPiece() != null && e.getNewArmorPiece().getType() != Material.AIR
                && e.getNewArmorPiece().getItemMeta().hasDisplayName()) {
            playerWhoEquipped.playSound(playerWhoEquipped.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
            playerWhoEquipped.sendMessage(ChatColor.GREEN + "You equipped "
                    + e.getNewArmorPiece().getItemMeta().getDisplayName() + "§a!");
        }
        if (e.getNewArmorPiece() != null && e.getNewArmorPiece().getType() != Material.AIR
                && !(e.getNewArmorPiece().getItemMeta().hasDisplayName())) {
            playerWhoEquipped.playSound(playerWhoEquipped.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1);
            playerWhoEquipped.sendMessage(ChatColor.GREEN + "You equipped " + e.getNewArmorPiece().getType() + "§a!");
        }
        final double previousMaxHealth = playerWhoEquipped.getMaxHealth();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                e.getPlayer().setHealthScale((playerWhoEquipped.getMaxHealth() / 12.5));//(50 / 12.5) = 4.0 = 2 hearts
                if (previousMaxHealth != playerWhoEquipped.getMaxHealth()) {
                    playerWhoEquipped.sendMessage
                            (ChatColor.YELLOW + "Your total health is now "
                                    + ChatColor.GREEN + ((int) playerWhoEquipped.getMaxHealth()) + "§e.");
                }
            }
        }, 10);//1 tick(s) = 0.5s
    }
}
