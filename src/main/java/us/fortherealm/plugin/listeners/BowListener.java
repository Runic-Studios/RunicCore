package us.fortherealm.plugin.listeners;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
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

@SuppressWarnings("FieldCanBeLocal")
public class BowListener implements Listener {

    private Plugin plugin = Main.getInstance();
    private static final int ARROW_VELOCITY_MULT = 3;
    private static double DAMAGE_AMT = 0.0;
    private static double speed = 0.0;

    @EventHandler
    public void onDraw(PlayerInteractEvent e) {

        // null check
        if (e.getItem() == null) { return; }

        // iterate through the bow's attributes, retrieve the weapon damage
        NBTItem nbti = new NBTItem(e.getItem());
        NBTList list = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        for(int i = 0; i < list.size(); i++){
            NBTListCompound lc = list.getCompound(i);
            if(lc.getString("Name").equals("custom.bowDamage")){
                DAMAGE_AMT = lc.getDouble("Amount");
            }
        }

        // iterate through the bow's attriutes, retrieve the weapon speed
        for(int i = 0; i < list.size(); i++){
            NBTListCompound lc = list.getCompound(i);
            if(lc.getString("Name").equals("custom.bowSpeed")){
                speed = lc.getDouble("Amount");
            }
        }

        Player player = e.getPlayer();

        if (e.getItem().getType() == Material.BOW) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

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

                    player.setCooldown(Material.BOW, (int) speed*20);

                }
            }
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        e.setCancelled(true);
    }

    // method to handle custom damage for bows
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {

            // cancel the event
            e.setCancelled(true);

            // grab our variables
            Player pl = (Player)e.getEntity();
            Arrow arrow = (Arrow) e.getDamager();
            Player damager = (Player) arrow.getShooter();

            // remove the arrow
            new BukkitRunnable(){
                public void run() {
                    ((CraftPlayer)pl).getHandle().getDataWatcher().set(new DataWatcherObject(10, DataWatcherRegistry.b), (Object)0);
                }
            }.runTaskLater(Main.getInstance(), 3);

            // damage the entity
            if (!(e.getEntity().getType().isAlive())) { return; }
            Damageable victim = (Damageable) e.getEntity();
            victim.damage((int) (DAMAGE_AMT/2), damager);
        }
    }

    // removes arrows stuck in bodies
    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            e.getEntity().remove();
        }
    }
}
