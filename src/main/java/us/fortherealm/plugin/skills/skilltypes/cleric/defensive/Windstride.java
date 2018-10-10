//package us.fortherealm.plugin.skills.skilltypes.defensive;
//
//import org.bukkit.*;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import us.fortherealm.plugin.Main;
//import us.fortherealm.plugin.skills.Skill;
//import SkillImpactEvent;
//
//public class Windstride extends Skill {
//
//    private static int DURATION = 10;
//    private static int SPEED_AMPLIFIER = 1;
//
//    public Windstride() {
//        super("Windstride", "You increase the movement speed of yourself and all party members by 50 units.");
//    }
//
//    @Override
//    public void executeSkill() {
//
//        // TODO get party and speed boost everyone
//
//        // Begin sound effects
//        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5F, 0.7F);
//        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 0.7F);
//
//        // Check if skill should impact
//        SkillImpactEvent event = new SkillImpactEvent(this);
//        Bukkit.getServer().getPluginManager().callEvent(event);
//
//        // Check if event cancels skill
//        if(event.isCancelled())
//            return;
//
//        // Send player info message
//        getPlayer().sendMessage(ChatColor.GREEN + "You feel the wind at your back!");
//
//        // Add player effects
//        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION *  20, SPEED_AMPLIFIER));
//
//        // Begin system to remove effects
//        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
//
//            // Removes effects and warns player
//            getPlayer().sendMessage(ChatColor.GRAY + "The strength of the wind leaves you.");
//            getPlayer().removePotionEffect(PotionEffectType.SPEED);
//
//            // Removes the skill from active skills
//            Skill.delActiveSkill(this);
//        }, DURATION *20);
//    }
//
//}
