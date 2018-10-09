//package us.fortherealm.plugin.skills.skilltypes.rogue.offensive.backstab;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Particle;
//import org.bukkit.Sound;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import us.fortherealm.plugin.skills.Skill;
//import us.fortherealm.plugin.skills.events.SkillImpactEvent;
//
//public class BackstabListener implements Listener {
//
//    @EventHandler
//    public void onDamage(EntityDamageByEntityEvent e) {
//        if (!(e.getDamager() instanceof Player))
//            return;
//        Player damager = (Player) e.getDamager();
//
//        for (Skill skill : Skill.getActiveSkills()) {
//            if (!(skill instanceof Backstab))
//                continue;
//
//            if (!(e.getDamager().equals(skill.getPlayer())))
//                continue;
//
//            if (damager.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) < 0.0D)
//                return;
//
//            SkillImpactEvent event = new SkillImpactEvent(skill);
//            Bukkit.getServer().getPluginManager().callEvent(event);
//
//            if(event.isCancelled())
//                return;
//
//            e.setDamage(e.getDamage() * 1.5);
//            e.getEntity().getWorld().spawnParticle(Particle.CRIT,
//                    e.getEntity().getLocation().add(0, 1.5, 0), 30, 0, 0.2F, 0.2F, 0.2F);
//            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 0.6F);
//
//            return;
//        }
//    }
//
//}
