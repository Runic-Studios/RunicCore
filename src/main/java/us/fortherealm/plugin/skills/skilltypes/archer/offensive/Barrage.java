package us.fortherealm.plugin.skills.skilltypes.archer.offensive;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

import java.util.UUID;

import static us.fortherealm.plugin.skills.skillutil.VectorUtil.rotateVectorAroundY;

public class Barrage extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    // constructor
    public Barrage() {
        super("Barrage", "shoot five arrows", 8, false);
    }

    @Override
    public void executeSkill() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector middle = getPlayer().getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector leftMid = rotateVectorAroundY(middle, -11.25);
        Vector rightMid = rotateVectorAroundY(middle, 11.25);
        Vector right = rotateVectorAroundY(middle, 22.5);

        startTask(getPlayer(), new Vector[]{middle, left, leftMid, rightMid, right});
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public Skill getSkill() {
        return this;
    }

    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {
        return true;
    }


    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {

    }

    @Override
    public void doImpact(EntityDamageByEntityEvent event) {

    }

    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            //if (arrow.)
            //bArrows.put(arrow, uuid);
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
}

//    @EventHandler(priority = EventPriority.HIGH)
//    public void onArrowDamage(EntityDamageByEntityEvent e) {
//        if (e.getDamager() instanceof Arrow) {
//            Arrow arrow = (Arrow) e.getDamager();
//            if (arrow.getShooter() instanceof Player) {
//                Player damager = (Player) arrow.getShooter();
//                if (bArrows.containsKey(arrow)) {
//                    e.setDamage(e.getDamage() * 2);
//                    e.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, e.getEntity().getLocation(), 3, 0, 0, 0, 0);
//                    e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);
//                    if (e.getEntity() instanceof Player) {
//                        Player victim = (Player) e.getEntity();
//                        victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 2));
//                        victim.sendMessage(ChatColor.RED + "You are dazed by " + ChatColor.WHITE + damager.getName()
//                                + ChatColor.RED + "'s barrage of arrows!");
//                    }
//                }
//            }
//        }
//    }
