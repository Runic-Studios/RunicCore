//package us.fortherealm.plugin.skills.skills;
//
//import us.fortherealm.plugin.oldskills.skilltypes.Skill;
//import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
//import org.bukkit.*;
//import org.bukkit.entity.*;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.skillutil.Vector;
//
//import java.skillutil.HashMap;
//import java.skillutil.UUID;
//
//// TODO: party damage check
//public class Barrage extends Skill {
//
//    private HashMap<Arrow, UUID> bArrows = new HashMap<>();
//    //hello
//    public Barrage() {
//        super("Barrage", "fire arrows that deal double damage and daze enemies", ChatColor.WHITE, ClickType.LEFT_CLICK_ONLY, 1);
//    }
//
//    @Override
//    public void onLeftClick(Player player, SkillItemType type) {
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5f, 1);
//        Vector middle = player.getEyeLocation().getDirection().normalize().multiply(2);
//        Vector left = rotateVectorAroundY(middle, -22.5);
//        Vector leftMid = rotateVectorAroundY(middle, -11.25);
//        Vector rightMid = rotateVectorAroundY(middle, 11.25);
//        Vector right = rotateVectorAroundY(middle, 22.5);
//
//        startTask(player, new Vector[]{middle, left, leftMid, rightMid, right});
//    }
//
//    private void startTask(Player player, Vector[] vectors) {
//        for (Vector vector : vectors) {
//            Arrow arrow = player.launchProjectile(Arrow.class);
//            UUID uuid = player.getUniqueId();
//            arrow.setVelocity(vector);
//            arrow.setShooter(player);
//            bArrows.put(arrow, uuid);
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    Location arrowLoc = arrow.getLocation();
//                    player.getWorld().spawnParticle(Particle.FLAME, arrowLoc, 5, 0, 0, 0, 0);
//                    if(arrow.isDead() || arrow.isOnGround()) {
//                        this.cancel();
//                    }
//                }
//            }.runTaskTimer(plugin, 0, 1L);
//        }
//    }
//
//    private Vector rotateVectorAroundY(Vector vector, double degrees) {
//        Vector newVector = vector.clone();
//        double rad = Math.toRadians(degrees);
//        double cos = Math.cos(rad);
//        double sine = Math.sin(rad);
//        double x = vector.getX();
//        double z = vector.getZ();
//        newVector.setX(cos * x - sine * z);
//        newVector.setZ(sine * x + cos * z);
//        return newVector;
//    }
//
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
//}
//
//
