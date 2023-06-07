package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamage;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Slam extends Spell implements PhysicalDamageSpell, RadiusSpell {
    private static final double KNOCKUP_AMT = 0.2;
    private static final double HEIGHT = 1.2;
    private final boolean ignite;
    private final Map<UUID, BukkitTask> slamTasks = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double radius;

    public Slam() {
        super("Slam", CharacterClass.WARRIOR);
        ignite = false;
        this.setDescription("You charge fearlessly into the air! " +
                "Upon hitting the ground, you deal (" +
                damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage to enemies within " +
                radius + " blocks and knock them up!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        // sounds, particles
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);

        // apply effects
        final Vector velocity = player.getVelocity().setY(HEIGHT);
        Vector directionVector = player.getLocation().getDirection();
        directionVector.setY(0);
        directionVector.normalize();

        float pitch = player.getEyeLocation().getPitch();
        if (pitch > 0.0F) {
            pitch = -pitch;
        }

        float multiplier = (90.0F + pitch) / 50.0F;
        directionVector.multiply(multiplier);
        velocity.add(directionVector);
        velocity.multiply(new Vector(0.6D, 0.8D, 0.6D));

        player.setVelocity(velocity);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
            if (slamTasks.get(player.getUniqueId()) != null)
                slamTasks.get(player.getUniqueId()).cancel();
        }, 120L); // insurance

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(new Vector
                        (player.getLocation().getDirection().getX(), -10.0,
                                player.getLocation().getDirection().getZ()).multiply(2).normalize());
                slamTasks.put(player.getUniqueId(), startSlamTask(player));
            }
        }.runTaskLater(RunicCore.getInstance(), 20L);
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EnvironmentDamage event) {
        if (!slamTasks.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == EnvironmentDamage.DamageCauses.FALL_DAMAGE) {
            event.setCancelled(true);
            slamTasks.remove(event.getVictim().getUniqueId());
        }
    }

    private BukkitTask startSlamTask(Player player) {
        Spell spell = this;
        return new BukkitRunnable() {
            @Override
            public void run() {

                if (player.isOnGround() || player.getFallDistance() == 1) {

                    this.cancel();
                    SlamLandEvent slamLandEvent = new SlamLandEvent(player);
                    Bukkit.getPluginManager().callEvent(slamLandEvent);
                    if (slamLandEvent.isCancelled) return;
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
                    player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                            25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

                    for (Entity en : player.getNearbyEntities(radius, radius, radius)) {
                        if (isValidEnemy(player, en)) {
                            DamageUtil.damageEntityPhysical(damage, (LivingEntity) en, player, false, false, spell);
                            Vector force = (player.getLocation().toVector().subtract
                                    (en.getLocation().toVector()).multiply(0).setY(KNOCKUP_AMT));
                            en.setVelocity(force.normalize());
                            if (ignite) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                                    DamageUtil.damageEntitySpell(damage, (LivingEntity) en, player, spell);
                                    en.getWorld().spawnParticle
                                            (Particle.LAVA, ((LivingEntity) en).getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                                }, 20L);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }

    /**
     * This custom event is called when the player lands
     */
    public static class SlamLandEvent extends Event implements Cancellable {
        private static final HandlerList handlers = new HandlerList();
        private final Player caster;
        private boolean isCancelled;

        /**
         * @param caster player who cast heal spell
         */
        public SlamLandEvent(Player caster) {
            this.caster = caster;
            this.isCancelled = false;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        public Player getCaster() {
            return this.caster;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override
        public void setCancelled(boolean arg0) {
            this.isCancelled = arg0;
        }
    }

}
