package us.fortherealm.plugin.skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.util.KnockbackUtil;

public class Fireball extends TargetingSkill<LivingEntity> implements Listener {
    
    private final double fireballSpeed = 2;
    private final int damageAmount = 20;
    
    private SmallFireball fireball;
    
    public Fireball() {
        super ("Fireball", "Shoots a fireball.");
    }
    
    public void executeSkill(Player player) {
        // Launch fireball
        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(fireballSpeed);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        
        // Sets the skill's fireball to the created fireball
        this.fireball = fireball;
        
        // play sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }
    
    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof SmallFireball))
            return;
        
        // Check if this fireball is the skill's fireball
        SmallFireball smfb = (SmallFireball) event.getDamager();
        if (!(smfb.equals(fireball)))
            return;
        
        // Cancel the original event to create our own effect
        event.setCancelled(true);
        
        // Tells skill who the target is
        LivingEntity target = (LivingEntity) event.getEntity();
        this.setTarget(target);
    
        // Tells events that the skill is about to impact
        SkillImpactEvent skillImpactEvent = new SkillImpactEvent(this);
        Bukkit.getPluginManager().callEvent(skillImpactEvent);
        
        if(skillImpactEvent.isCancelled())
            return;
        
        // perform damage
        target.damage(damageAmount, player);
        target.setLastDamageCause(event);
        KnockbackUtil.knockback(player, target);

        // effects
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        target.getWorld().spigot().playEffect(target.getEyeLocation(),
                Effect.FLAME, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);

    }
}
