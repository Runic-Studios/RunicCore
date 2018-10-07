//package us.fortherealm.plugin.skills.skilltypes.rogue.offensive.backstab;
//
//import org.bukkit.*;
//import org.bukkit.scheduler.BukkitRunnable;
//import us.fortherealm.plugin.Main;
//import us.fortherealm.plugin.skills.Skill;
//
//import java.util.UUID;
//
//public class Backstab extends Skill {
//
//    public Backstab() {
//        super("Backstab", "Self buff. For the duration, striking enemies from behind deals 150% dmg");
//    }
//
//    @Override
//    public void executeSkill() {
//        getPlayer().sendMessage(ChatColor.GREEN + "You are now backstabbing!");
//        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 0.5f, 1.0f);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Skill.delActiveSkill(Backstab.this);
//                getPlayer().sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
//            }
//        }.runTaskLater(Main.getInstance(), 200L);
//    }
//}
