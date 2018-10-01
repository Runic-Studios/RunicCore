package us.fortherealm.plugin.skills.skilltypes.defensive;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.util.Bubble;

public class Deliverance extends TargetingSkill<Player> {
    
    private final int walkSpeed = 0;
    private final int bubbleDuration = 8;
    private final int confusionLevel = 2;
    private final int bubbleSize = 5;
    private final int updatesPerSecond = 10;
    
    private float initialWalkSpeed;
    
    public Deliverance() {
        super(
                "Deliverance",
                "Summon a barrier of holy power around yourself for 8 seconds." +
                        " The barrier repels all enemies, however allies may pass through the barrier freely." +
                        " During this time, you may not move",
                false);
    }

    @Override
    public void executeSkill() {
        
        initialWalkSpeed = player.getWalkSpeed();
        
        // Set player effects
        player.setWalkSpeed(walkSpeed);
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.CONFUSION,
                        bubbleDuration*20,
                        confusionLevel
                )
        );
        
        // Create bubble around player
        Bubble.bubbleEffect(player.getLocation(), Particle.FIREWORKS_SPARK,
                10 /* 5 oscillations */, 0, 1, bubbleSize);
        
        // Play sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getLocation().getWorld().spigot().strikeLightningEffect(player.getLocation(), true);
        
        // Begin skill event
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                
                // Skill duration
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > bubbleDuration * 1000) {
                    this.cancel();
                    player.setWalkSpeed(initialWalkSpeed);
                    return;
                }
    
                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);
    
                // Look for targets nearby
                for (Entity entity : player.getLocation().getChunk().getEntities()) {
    
                    // Removes targets not close enough
                    if (entity.getLocation().distance(player.getLocation()) > bubbleSize ||
                            !(entity instanceof Player))
                        continue; // Continue ends the current for loop iteration and moves on to the next
    
                    // Tells skill who the target is
                    Player target = (Player) entity;

                    if(player.equals(target))
                        continue;

                    Deliverance.this.setTarget(target);
                    
                    // Tells events that the skill is about to impact
                    SkillImpactEvent event = new SkillImpactEvent(Deliverance.this);
                    Bukkit.getPluginManager().callEvent(event);
                    
                    // Checks if the event cancelled the skill
                    if(event.isCancelled())
                        continue;
                    
                    // Executes the skill
                    Vector force = (player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-0.75).setY(0.3));
                    entity.setVelocity(force);
                }
            }
        }.runTaskTimer(getPlugin(), 0, 20/updatesPerSecond);

        player.setWalkSpeed(initialWalkSpeed);
    }
}