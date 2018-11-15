package us.fortherealm.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;
import net.minecraft.server.v1_13_R2.DataWatcherObject;
import net.minecraft.server.v1_13_R2.DataWatcherRegistry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
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

    private Plugin plugin = Main.getInstance();
    private static final int ARROW_VELOCITY_MULT = 3;

    @EventHandler
    public void onDraw(PlayerInteractEvent e) {

        Player player = e.getPlayer();

        if (e.getItem() != null && e.getItem().getType() == Material.BOW) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                // cancel the default bow mechanics
                e.setCancelled(true);

                // don't fire arrow if they're sneaking, since they're casting a spell
                if (player.isSneaking()) {
                    return;
                }

                // implement cooldown system
                if (player.getCooldown(Material.BOW) <= 0) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);

                    // fire a custom arrow
                    final Vector direction = player.getEyeLocation().getDirection().multiply(ARROW_VELOCITY_MULT);

                    Arrow myArrow = player.getWorld().spawn
                            (player.getEyeLocation().add
                                    (direction.getX(), direction.getY(), direction.getZ()), Arrow.class);

                    myArrow.setVelocity(direction);

                    myArrow.setShooter(player);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location arrowLoc = myArrow.getLocation();
                            player.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 5, 0, 0, 0, 0);
                            if(myArrow.isDead() || myArrow.isOnGround()) {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0, 1L);

                    player.setCooldown(Material.BOW, 15);

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
            Arrow arrow = (Arrow) e.getDamager();
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
