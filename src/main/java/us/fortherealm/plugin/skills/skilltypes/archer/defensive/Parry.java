package us.fortherealm.plugin.skills.skilltypes.archer.defensive;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;

import java.util.HashMap;
import java.util.UUID;

public class Parry extends Skill implements Listener {

    // globals (eventually I'd like to move the events back in here)
    private static HashMap<UUID, Long> noFall = new HashMap<>();
    private double LAUNCH_PATH_MULT = 1.5;

    // constructor
    public Parry() {
        super("Parry", "you parry", 1);
    }

    @Override
    public void executeSkill() {

        // skill variables, vectors
        UUID uuid = getPlayer().getUniqueId();
        Vector look = getPlayer().getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        // particles, sounds
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        getPlayer().getWorld().spawnParticle(Particle.REDSTONE, getPlayer().getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.GRAY, 20));

        getPlayer().setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));
        noFall.put(uuid, System.currentTimeMillis());

        // remove the player from the noFall hashmap
        new BukkitRunnable() {
            @Override
            public void run() {
                noFall.remove(uuid);
                getPlayer().sendMessage(ChatColor.GRAY + "You lost safefall!");
            }
        }.runTaskLater(Main.getInstance(), 40L);
    }

    public HashMap<UUID, Long> getNoFall() {
        return noFall;
    }
//
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onFallDamage(EntityDamageEvent e) {
//        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
//            e.getEntity().sendMessage(e.getEntity().getUniqueId() + "");
//            e.getEntity().sendMessage("ahh");
//            UUID uuid = e.getEntity().getUniqueId();
//            if (noFall.containsKey(uuid)) {
//                e.setCancelled(true);
//                e.getEntity().setFallDistance(-500f);
//                e.getEntity().sendMessage("you were prevented from fall damage");
//            }
//        }
//    }
//
//    @EventHandler
//    public void onQuit(PlayerQuitEvent e) {
//        UUID uuid = e.getPlayer().getUniqueId();
//        noFall.remove(uuid);
//    }
}