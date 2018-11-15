package us.fortherealm.plugin.skills.skilltypes.rogue.offensive;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

public class Backstab extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    private static final double DURATION = 10;

    public Backstab() {
        super("Backstab", "Self buff. For the " + DURATION + "s duration, striking enemies from behind deals 150% dmg", 5, false);
    }

    @Override
    public void executeSkill() {

        // start the buff
        getPlayer().sendMessage(ChatColor.GREEN + "You are now backstabbing!");
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {

        // listen for our exact scenario
        if (!(event.getDamager().equals(getPlayer())))
            return false;

        // check that the player is standing behind the entity
        return getPlayer().getLocation().getDirection().dot(event.getEntity().getLocation().getDirection()) > 0.0D;
    }

    @Override
    public void initializeSkillVariables(EntityDamageByEntityEvent event) {

        // Sets the target, reminder to initialize variables
        LivingEntity target = (LivingEntity) event.getEntity();

        if(target == null)
            return;

        setTarget(target);
    }

    @Override
    public Skill getSkill() {
        return this;
    }

    @Override
    public void doImpact(EntityDamageByEntityEvent event) {
        event.setDamage(event.getDamage() * 1.5);
        event.getEntity().getWorld().spawnParticle(Particle.CRIT,
                event.getEntity().getLocation().add(0, 1.5, 0), 30, 0, 0.2F, 0.2F, 0.2F);
        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 0.5F, 0.6F);
    }

    @Override
    public double timeUntilRemoval() {
        return DURATION;
    }

    @Override
    public boolean removeAfterImpact() {
        return false;
    }

    @Override
    public void onRemoval() {

        // end of the buff
        getPlayer().sendMessage(ChatColor.GRAY + "You are no longer backstabbing.");
    }
}
