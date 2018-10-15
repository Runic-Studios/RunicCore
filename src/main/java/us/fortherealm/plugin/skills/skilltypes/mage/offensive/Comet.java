package us.fortherealm.plugin.skills.skilltypes.mage.offensive;

import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.fortherealm.plugin.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.listeners.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;

public class Comet extends TargetingSkill<EntityDamageByEntityEvent> implements ImpactListener<EntityChangeBlockEvent> {

    // global variables
    private FallingBlock comet;
    private static final double COMET_SPEED = 0.1;
    private static final int DAMAGE_AMT = 20;
    private static final int BLAST_RADIUS = 5;
    private static final int MAX_DIST = 10;
    private static final double KNOCKBACK_MULT = -0.5;

    // default constructor
    public Comet() {
        super("Comet", "coming soon", false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void executeSkill() {

        // play effects, spawn the comet
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1.0F);
        Block targetBlock = getPlayer().getTargetBlock(null, MAX_DIST);
        Location targetLoc = targetBlock.getLocation().clone().add(0, 30, 0);
        FallingBlock comet = targetLoc.getWorld().spawnFallingBlock(targetLoc, Material.DRAGON_EGG, (byte) 0);
        comet.setDropItem(false);

        // set the comet's trajectory
        Vector trajectory = targetBlock.getLocation().toVector().subtract(comet.getLocation().toVector());
        comet.setVelocity(trajectory.multiply(COMET_SPEED));

        // start the runnable
        new BukkitRunnable() {
            @Override
            @SuppressWarnings("deprecation")
            public void run() {

                //more particle effects
                comet.getWorld().spawnParticle(Particle.FLAME, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 5, 0, 1.0F, 0, 0);

                // once the comet lands or despawns
                if ((comet.isOnGround() || comet.isDead())) {

                    this.cancel();

                    // play effects
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
                    comet.getWorld().playSound(comet.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 1.0F);
                    comet.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, comet.getLocation(), 5, 0.2F, 1.0F, 0.2F, 0);
                    comet.getWorld().spawnParticle(Particle.FLAME, comet.getLocation(), 45, 1F, 1F, 1F, 0);
                    comet.getWorld().spawnParticle(Particle.SMOKE_LARGE, comet.getLocation(), 45, 1F, 1F, 1F, 0);

                    // get nearby enemies within blast radius
                    for (Entity entity : comet.getNearbyEntities(BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS)) {

                        // apply effects, damage
                        if (entity != getPlayer() && entity.getType().isAlive()) {
                            Damageable victim = (Damageable) entity;
                            victim.damage(DAMAGE_AMT, getPlayer());
                            //KnockbackUtil.knockback(getPlayer(), victim, 2);
                            Vector knockback = (comet.getLocation().toVector().subtract(victim.getLocation().toVector())
                                    .multiply(KNOCKBACK_MULT).setY(0.3333));
                            victim.setVelocity(knockback);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getInstance(), 0L, 1L);

        // sets our comet object's name variable to this method's name variable
        this.comet = comet;
    }

    @Override
    public Class<EntityChangeBlockEvent> getEventClass() {
        return EntityChangeBlockEvent.class;
    }

    @Override
    public Skill getSkill() {
        return this;
    }

    @Override
    public boolean isPreciseEvent(EntityChangeBlockEvent event) {

        // make sure the skill is listening for our exact event
        if (!(event.getEntity() instanceof FallingBlock))
            return false;

        FallingBlock cmt = (FallingBlock) event.getEntity();

        if(cmt == null)
            return false;

        return cmt.equals(comet);
    }

    @Override
    public void initializeSkillVariables(EntityChangeBlockEvent event) {

        // TODO: should this set all entites damaged by the comet to its targets? If so, just needs a radius check similar to doImpact
        // Sets the target, reminder to initialize variables
        // for...
        //LivingEntity target = (LivingEntity) event.getEntity();

        //if(target == null)
        //return;

        //setTarget(target);
    }

    @Override
    public void doImpact(EntityChangeBlockEvent event) {

        // if the comet tries to change a block, stop it.
        event.setCancelled(true);
    }
}
