package us.fortherealm.plugin.events;

import us.fortherealm.plugin.Main;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FireBowEvent implements Listener {

    @EventHandler
    public void onDraw(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getItem() != null && e.getItem().getType() == Material.BOW) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                if (player.getCooldown(Material.BOW) <= 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
                    Arrow arrow = player.launchProjectile(Arrow.class); // fix this projectile
                    final Vector velocity = player.getLocation().getDirection().normalize().multiply(2);
                    arrow.setVelocity(velocity);
                    arrow.setShooter(player);
                    player.setCooldown(Material.BOW, 20); // 1 second
                } else if (player.getCooldown(Material.BOW) != 0) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
            new BukkitRunnable(){
                public void run() {
                    Player p = (Player)e.getEntity();
                    ((CraftPlayer)p).getHandle().getDataWatcher().set(new DataWatcherObject(10, DataWatcherRegistry.b), (Object)0);
                }
            }.runTaskLater(Main.getInstance(), 3);
        }
    }

    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            e.getEntity().remove();
        }
    }
}
