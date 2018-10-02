package us.fortherealm.plugin.skills.skilltypes.runic.offensive;

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
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.events.SkillImpactEvent;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.util.KnockbackUtil;

public class Fireball extends TargetingSkill<LivingEntity> {

    private SmallFireball fireball;

    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 20;
    
    public Fireball() {
        super ("Fireball", "Shoots a fireball.");
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

    @EventHandler
    public void onFireballDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof SmallFireball))
            return;

        for(Skill skill : Skill.getActiveSkills()) {

            Player player = skill.getPlayer();

            if(!(skill instanceof Fireball))
                continue;
            Fireball fireball = (Fireball) skill;

            SmallFireball smfb = (SmallFireball) event.getDamager();
            if(fireball.getSmallFireball() == null)
                continue;

            if(!(smfb.equals(fireball.getSmallFireball())))
                continue;

            // Cancel the original event to create our own effect
            event.setCancelled(true);

            // Tells skill who the target is
            LivingEntity target = (LivingEntity) event.getEntity();

            if(target == null)
                continue;

            fireball.setTarget(target);

            // Tells events that the skill is about to impact
            SkillImpactEvent skillImpactEvent = new SkillImpactEvent(fireball);
            Bukkit.getPluginManager().callEvent(skillImpactEvent);

            if(skillImpactEvent.isCancelled())
                return;

            // preform damage
            target.damage(((Fireball) skill).getDamageAmount(), player);
            target.setLastDamageCause(event);
            KnockbackUtil.knockback(player, target);

            // effects
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            target.getWorld().spigot().playEffect(target.getEyeLocation(),
                    Effect.FLAME, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 50, 16);

            // remove skill from active skills
            Skill.delActiveSkill(skill);
            return;

        }

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
