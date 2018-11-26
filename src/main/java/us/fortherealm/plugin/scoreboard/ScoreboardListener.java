package us.fortherealm.plugin.scoreboard;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

public class ScoreboardListener implements Listener {

    // global variables
    private ScoreboardHandler sbh = new ScoreboardHandler();
    private Plugin plugin = Main.getInstance();

    @EventHandler
    public void onDamage (EntityDamageEvent e) {

        // only listen for players
        if (!(e.getEntity() instanceof Player)) { return; }

        Player pl = (Player) e.getEntity();

        // null check
        if (pl.getScoreboard() == null) { return; }

        updateHealth(pl);
    }

    @EventHandler
    public void onRegen (EntityRegainHealthEvent e) {

        //only listen for players
        if (!(e.getEntity() instanceof Player)) { return; }
        Player pl = (Player) e.getEntity();

        // null check
        if (pl.getScoreboard() == null) { return; }

        updateHealth(pl);
    }

    @EventHandler
    public void onArmorEquip (ArmorEquipEvent e) {

        Player pl = e.getPlayer();

        // null check
        if (pl.getScoreboard() == null) { return; }

        updateHealth(pl);

        final double OLD_MAX_HEALTH = pl.getMaxHealth();

        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().setHealthScale((pl.getMaxHealth() / 12.5));//(50 / 12.5) = 4.0 = 2 hearts
                if (OLD_MAX_HEALTH != pl.getMaxHealth()) {
                    pl.sendMessage
                            (ChatColor.YELLOW + "Your total health is now "
                                    + ChatColor.GREEN + ((int) pl.getMaxHealth()) + "§e.");
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private void updateHealth(Player pl) {

        // update health bar and scoreboard
        new BukkitRunnable() {
            @Override
            public void run() {
                sbh.updateSideInfo(pl);
                sbh.updateHealthbar(pl);
            }
        }.runTaskLater(plugin, 1);
    }
}
