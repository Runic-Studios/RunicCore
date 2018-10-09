package us.fortherealm.plugin.skills.skilltypes.runic.offensive;

import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.formats.VertCircleFrame;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.listeners.ImpactListener;
import us.fortherealm.plugin.skills.skilltypes.TargetingSkill;
import us.fortherealm.plugin.skills.util.KnockbackUtil;

public class Frostbolt extends TargetingSkill<LivingEntity> implements ImpactListener<EntityDamageByEntityEvent> {

    private Snowball snowball;
    private static final double SNOWBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 10;

    // default constructor
    public Frostbolt() {
        super("Frostbolt", "Shoots a frostbolt.", false);
    }

    @Override
    public void executeSkill() {
        // launch our snowball
        Snowball snowball = getPlayer().launchProjectile(Snowball.class);
        final Vector velocity = getPlayer().getLocation().getDirection().normalize().multiply(SNOWBALL_SPEED);
        snowball.setVelocity(velocity);
        snowball.setShooter(getPlayer());
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5f, 1);

        // premium particle effect
        new BukkitRunnable() {
            public void run() {
                new VertCircleFrame(0.5F).playParticle(Particle.SNOW_SHOVEL, snowball.getLocation());
                if (snowball.isOnGround() || snowball.isDead()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

        // sets the objects name variable to the method's name variable
        this.snowball = snowball;
    }

    @Override
    public Class<EntityDamageByEntityEvent> getEventClass() {
        return EntityDamageByEntityEvent.class;
    }

    // returns the skill
    @Override
    public Skill getSkill() {
        return this;
    }

    // listen for this specific snowball
    @Override
    public boolean isPreciseEvent(EntityDamageByEntityEvent event) {
        // make sure the skill is listening for the exact event
        if (!(event.getDamager() instanceof Snowball))
            return false;

        Snowball sb = (Snowball) event.getDamager();
        if(snowball == null)
            return false;

        return sb.equals(snowball);
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
    public void doImpact(EntityDamageByEntityEvent event) {
        // Cancel the original event to create our own effect
        event.setCancelled(true);

        LivingEntity target = this.getTarget();

        // perform damage & effects
        target.damage(DAMAGE_AMOUNT, getPlayer());
        target.setLastDamageCause(event);
        KnockbackUtil.knockback(getPlayer(), target);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
        if (target instanceof Player) {
            target.sendMessage(ChatColor.RED + "You are slowed by " + ChatColor.WHITE +
                    getPlayer().getName() + "§c's frostbolt!");
        }

        // general effects
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1);
        target.getWorld().spawnParticle(Particle.SNOWBALL, target.getEyeLocation(), 5, 0.3f, 0.3f, 0.3f);
    }
}

