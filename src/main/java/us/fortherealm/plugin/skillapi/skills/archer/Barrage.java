package us.fortherealm.plugin.skillapi.skills.archer;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Barrage extends Skill {

    // globals
    private HashMap<Arrow, UUID> bArrows = new HashMap<>();
    private static final int DAMAGE_MULTIPLIER = 2;

    // constructor
    public Barrage() {
        super("Barrage",
                "You launch a volley of five magical arrows\nthat deal " + (DAMAGE_MULTIPLIER*100) + "% weapon damage!",
                ChatColor.WHITE, ClickType.LEFT_CLICK_ONLY, 1, 5);
    }

    // skill execute code
    @Override
    public void onLeftClick(Player pl, SkillItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector leftMid = rotateVectorAroundY(middle, -11.25);
        Vector rightMid = rotateVectorAroundY(middle, 11.25);
        Vector right = rotateVectorAroundY(middle, 22.5);
        startTask(pl, new Vector[]{middle, left, leftMid, rightMid, right});
    }

    // vectors, particles
    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            bArrows.put(arrow, uuid);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    player.getWorld().spawnParticle(Particle.FLAME, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 1L);
        }
    }

    // deal bonus damage if arrow is a barrage arrow
    @EventHandler
    public void onArrowDamage(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal extra damage if arrow in in the barrage hashmap
        if (bArrows.containsKey(arrow)) {
            e.setDamage(e.getDamage() + (e.getDamage() * DAMAGE_MULTIPLIER));
            e.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
        }
    }
}
