package us.fortherealm.plugin.scoreboard;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import us.fortherealm.plugin.Main;

public class ScoreboardHealthListener implements Listener {

    private ScoreboardManager sbm = new ScoreboardManager();
    private Plugin plugin = Main.getInstance();

    @EventHandler
    public void onDamage (EntityDamageEvent e) {

        //only listen for players
        if (!(e.getEntity() instanceof Player)) { return; }

        Player victim = (Player) e.getEntity();

        // set to 3 tick delay to ensure scoreboard displays proper health value
        new BukkitRunnable() {
            @Override
            public void run() {

                sbm.updateSideScoreboard(victim);

                // update health bar
                Objective healthbar = victim.getScoreboard().getObjective("healthbar");
                for (Player online : Bukkit.getOnlinePlayers()) {
                    Score score = healthbar.getScore(online.getName());
                    score.setScore((int) online.getHealth());
                }
            }
        }.runTaskLater(plugin, 3);
    }

    @EventHandler
    public void onRegen (EntityRegainHealthEvent e) {

        //only listen for players
        if (!(e.getEntity() instanceof Player)) { return; }

        Player victim = (Player) e.getEntity();

        // set to 3 tick delay to ensure scoreboard displays proper health value
        new BukkitRunnable() {
            @Override
            public void run() {

                sbm.updateSideScoreboard(victim);

                // update health bar
                Objective healthbar = victim.getScoreboard().getObjective("healthbar");
                for (Player online : Bukkit.getOnlinePlayers()) {
                    Score score = healthbar.getScore(online.getName());
                    score.setScore((int) online.getHealth());
                }
            }
        }.runTaskLater(plugin, 3);
    }

    @EventHandler
    public void onArmorEquip (ArmorEquipEvent e) {

        Player playerWhoEquipped = e.getPlayer();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sbm.updateSideScoreboard((Player) playerWhoEquipped);
                //sbm.updateHealthBar((Player) playerWhoEquipped);
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
