//package us.fortherealm.plugin.skills.skilltypes.warrior.defensive;
//
//import org.bukkit.Bukkit;
//import org.bukkit.Particle;
//import org.bukkit.Sound;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.Player;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.potion.PotionEffectType;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.util.Vector;
//import us.fortherealm.plugin.Main;
//import us.fortherealm.plugin.skills.Skill;
//import us.fortherealm.plugin.skills.events.SkillImpactEvent;
//import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
//import us.fortherealm.plugin.skills.formats.Bubble;
//
//public class Deliverance extends TargetingSkill<Player> {
//
//    private static final int SLOW_AMPLITUDE = 127;
//    private static final int BUBBLE_DURATION = 8;
//    private static final int CONFUSION_LEVEL = 2;
//    private static final int BUBBLE_SIZE = 5;
//    private static final int UPDATES_PER_SECOND = 10;
//
//    public Deliverance() {
//        super(
//                "Deliverance",
//                "Summon a barrier of holy power around yourself for 8 seconds." +
//                        " The barrier repels all enemies, however allies may pass through the barrier freely." +
//                        " During this time, you may not move",
//                false
//        );
//    }
//
//    @Override
//    public void executeSkill() {
//
//        // Set player effects
//        getPlayer().addPotionEffect(
//                new PotionEffect(
//                        PotionEffectType.SLOW,
//                        BUBBLE_DURATION,
//                        SLOW_AMPLITUDE
//                )
//        );
//        getPlayer().addPotionEffect(
//                new PotionEffect(
//                        PotionEffectType.CONFUSION,
//                        BUBBLE_DURATION *20,
//                        CONFUSION_LEVEL
//                )
//        );
//
//        // Create bubble around player
//        Bubble.bubbleEffect(getPlayer().getLocation(), Particle.FIREWORKS_SPARK,
//                10 /* 5 oscillations */, 0, 1, BUBBLE_SIZE);
//
//        // Play sound effects
//        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 0.5F, 1.0F);
//        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
//        getPlayer().getLocation().getWorld().spigot().strikeLightningEffect(getPlayer().getLocation(), true);
//
//        // Begin skill event
//        final long startTime = System.currentTimeMillis();
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//
//                // Skill duration
//                long timePassed = System.currentTimeMillis() - startTime;
//                if (timePassed > BUBBLE_DURATION * 1000) {
//                    this.cancel();
//                    getPlayer().removePotionEffect(PotionEffectType.SLOW);
//                    return;
//                }
//
//                // More effect noises
//                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);
//
//                // Look for targets nearby
//                for (Entity entity : getPlayer().getLocation().getChunk().getEntities()) {
//
//                    // Removes targets not close enough
//                    if (entity.getLocation().distance(getPlayer().getLocation()) > BUBBLE_SIZE ||
//                            !(entity instanceof Player))
//                        continue; // Continue ends the current for loop iteration and moves on to the next
//
//                    // Tells skill who the target is
//                    Player target = (Player) entity;
//
//                    if(getPlayer().equals(target))
//                        continue;
//
//                    Deliverance.this.setTarget(target);
//
//                    // Tells events that the skill is about to impact
//                    SkillImpactEvent event = new SkillImpactEvent(Deliverance.this);
//                    Bukkit.getPluginManager().callEvent(event);
//
//                    // Checks if the event cancelled the skill
//                    if(event.isCancelled())
//                        continue;
//
//                    // Executes the skill
//                    Vector force = (getPlayer().getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.75).setY(0.3));
//                    entity.setVelocity(force);
//
//                    // Removes the skill from the active skills list
//                    Skill.delActiveSkill(Deliverance.this);
//                }
//            }
//        }.runTaskTimer(Main.getInstance(), 0, 20/ UPDATES_PER_SECOND);
//    }
//}
