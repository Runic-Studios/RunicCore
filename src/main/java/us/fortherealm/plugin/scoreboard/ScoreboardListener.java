package us.fortherealm.plugin.scoreboard;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.player.utilities.HealthUtils;

public class ScoreboardListener implements Listener {

    // global variables
    private ScoreboardHandler sbh = FTRCore.getScoreboardHandler();
    private Plugin plugin = FTRCore.getInstance();

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
                HealthUtils.setHeartDisplay(pl);
                double maxHealth = pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
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
