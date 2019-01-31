package us.fortherealm.plugin.skillapi.skills.archer;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Parry extends Skill {

    // globals (eventually I'd like to move the events back in here)
    private HashMap<UUID, Long> noFall = new HashMap<>();
    private static final double LAUNCH_PATH_MULT = 1.5;
    private static final int DURATION = 2;

    // constructor
    public Parry() {
        super("Parry", "You launch yourself backwards in the air!" +
                "\nFor the next " + DURATION + " seconds, you are protected" +
                "\nfrom fall damage.", ChatColor.WHITE, 8, 5);
    }

    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        // skill variables, vectors
        UUID uuid = pl.getUniqueId();
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));
        noFall.put(uuid, System.currentTimeMillis());

        // remove the pl from the noFall hashmap
        new BukkitRunnable() {
            @Override
            public void run() {
                noFall.remove(uuid);
                pl.sendMessage(ChatColor.GRAY + "You lost safefall!");
            }
        }.runTaskLater(Main.getInstance(), (DURATION*20));
    }

    // prevent fall damage if the pl is parrying
    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            UUID uuid = e.getEntity().getUniqueId();
            if (noFall.containsKey(uuid)) {
                e.setCancelled(true);
            }
        }
    }

    // remove pl from the hashmap on logout
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        noFall.remove(uuid);
    }
}
