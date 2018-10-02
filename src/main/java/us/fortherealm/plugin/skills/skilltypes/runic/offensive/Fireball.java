package us.fortherealm.plugin.skills.skilltypes.runic.offensive;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

public class Fireball extends TargetingSkill<LivingEntity> {

    private SmallFireball fireball;

    private final double fireballSpeed = 2;
    private final int damageAmount = 20;
    
    public Fireball() {
        super ("Fireball", "Shoots a fireball.");
    }

    @Override
    public void executeSkill() {
        // Launch fireball
        SmallFireball fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(fireballSpeed);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        
        // play sound effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
        
        this.fireball = fireball;
    }

    public SmallFireball getSmallFireball() {
        return fireball;
    }

    public SmallFireball getFireball() {
        return fireball;
    }

    public double getFireballSpeed() {
        return fireballSpeed;
    }

    public int getDamageAmount() {
        return damageAmount;
    }
}
