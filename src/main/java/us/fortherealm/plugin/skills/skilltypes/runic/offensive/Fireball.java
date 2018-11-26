package us.fortherealm.plugin.skills.skilltypes.runic.offensive;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.parties.PartyManager;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.skillutil.KnockbackUtil;
import us.fortherealm.plugin.skills.skillutil.formats.VertCircleFrame;

public class Fireball extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 20;

    // WARNING: If a player casts a new fireball before the first one impacts,
    // this will override the first fireball and the first fireball will no
    // longer activate this skill.
    private SmallFireball fireball;

    public Fireball() {
        super("Fireball", "Shoots a fireball.", 8, false);
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

        // sets the objects name variable to the method's name variable
        this.fireball = fireball;
    }

    // java generics
    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {
        // make sure the skill is listening for the exact event
        if (!(event.getDamager() instanceof SmallFireball))
            return false;

        SmallFireball smfb = (SmallFireball) event.getDamager();
        if(fireball == null)
            return false;

        return smfb.equals(fireball);
    }

    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {
        // Sets the target, reminder to initialize variables
        LivingEntity target = (LivingEntity) event.getEntity();

        if(target == null)
            return;

        setTarget(target);
    }

    // returns the skill to the impact listener
    @Override
    public Skill getSkill() {
        return this;
    }

    @Override
    public void doImpact(EntityDamageByEntityEvent event) {

        // cancel the original event to create our own effect
        event.setCancelled(true);

        LivingEntity target = this.getTarget();

        // cancel the effect if the target is in our party
        if (Main.getPartyManager().getPlayerParty(getPlayer()) != null
                && Main.getPartyManager().getPlayerParty(getPlayer()).hasMember(target.getUniqueId())) { return; }

        // perform damage
        target.damage(DAMAGE_AMOUNT, getPlayer());
        target.setLastDamageCause(event);
        KnockbackUtil.knockback(getPlayer(), target, 1);

        // general effects
        getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        target.getWorld().spawnParticle(Particle.LAVA, target.getEyeLocation(), 5, 0.1f, 0.1f, 0.1f);
    }

}
