package us.fortherealm.plugin.scoreboard;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
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

        new BukkitRunnable() {
            @Override
            public void run() {
                //ex: (50 / 12.5) = 4.0 = 2 hearts displayed
                double maxHealth = pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                pl.setHealthScale((maxHealth / 12.5));
                if (pl.getHealth() > maxHealth) {
                    pl.setHealth(maxHealth);
                }
                updateHealth(pl);
            }
        }.runTaskLater(plugin, 1L);
    }

    private void updateHealth(Player pl) {

        // update health bar and scoreboard
        new BukkitRunnable() {
            @Override
            public void run() {
                sbh.updateSideInfo(pl);
            }
        }.runTaskLater(plugin, 1);
    }
}
