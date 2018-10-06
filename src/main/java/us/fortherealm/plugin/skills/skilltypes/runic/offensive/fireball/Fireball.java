package us.fortherealm.plugin.skills.skilltypes.runic.offensive.fireball;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

public class Fireball extends TargetingSkill<LivingEntity> {

    private SmallFireball fireball;

    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 20;

    static {
        // Initialize Fireball Listener
        Bukkit.getServer().getPluginManager().registerEvents(new FireballListener(), Main.getInstance());
    }

    public Fireball() {
        super("Fireball", "Shoots a fireball.", false);
    }

    @Override
    public void executeSkill() {
        // Launch fireball
        SmallFireball fireball = getPlayer().launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = getPlayer().getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(getPlayer());
        
        // play sound effect
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
        
        this.fireball = fireball;
    }

    public SmallFireball getSmallFireball() {
        return fireball;
    }

    public SmallFireball getFireball() {
        return fireball;
    }

    public double getFireballSpeed() {
        return FIREBALL_SPEED;
    }

    public int getDamageAmount() {
        return DAMAGE_AMOUNT;
    }
}
