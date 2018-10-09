//package us.fortherealm.plugin.skills.skilltypes.offensive.smokebomb;
//
//import org.bukkit.entity.Arrow;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import us.fortherealm.plugin.skills.Skill;
//
//public class SmokeBombListener implements Listener {
//
//    @EventHandler(priority = EventPriority.HIGH)
//    public void onArrowDamage(EntityDamageByEntityEvent e) {
//        if(!(e.getDamager() instanceof Arrow))
//            return;
//        for(Skill skill : Skill.getActiveSkills()) {
//            if((skill instanceof SmokeBomb))
//                continue;
//
//            if(!(e.getDamager().equals(((SmokeBomb) skill).getArrow())))
//                continue;
//
//
//
//            e.setCancelled(true);
//            Skill.delActiveSkill(skill);
//            return;
//        }
//    }
//
//}
