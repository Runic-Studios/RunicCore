package com.runicrealms.plugin.spellapi.spells.rogue;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;

public class Harpoon extends Spell implements DurationSpell, PhysicalDamageSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double tridentSpeed;
    private Trident trident;

    public Harpoon() {
        super("Harpoon", CharacterClass.ROGUE);
        this.setDescription("You launch a projectile harpoon of the sea! Upon hitting an enemy, " +
                "the trident deals (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage and pulls its target towards you, slowing them for " + duration + "s! " +
                "If an ally is hit, you are instead teleported to their location.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        trident = player.launchProjectile(Trident.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(tridentSpeed);
        trident.setDamage(0);
        trident.setVelocity(velocity);
        trident.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.75f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.5f);

        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trident.isDead() || trident.isOnGround()) {
                    this.cancel();
                    trident.remove();
                }
                trident.getWorld().spawnParticle(Particle.REDSTONE, trident.getLocation(),
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.TEAL, 1));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = (int) physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = (int) physicalDamagePerLevel;
    }

    @Override
    public void loadPhysicalData(Map<String, Object> spellData) {
        Number physicalDamage = (Number) spellData.getOrDefault("physical-damage", 0);
        setPhysicalDamage(physicalDamage.doubleValue());
        Number physicalDamagePerLevel = (Number) spellData.getOrDefault("physical-damage-per-level", 0);
        setPhysicalDamagePerLevel(physicalDamagePerLevel.doubleValue());
        Number tridentSpeed = (Number) spellData.getOrDefault("trident-speed", 0);
        setTridentSpeed(tridentSpeed.doubleValue());
    }

    @EventHandler
    public void onTridentDamage(ProjectileCollideEvent event) {
        if (!event.getEntity().equals(this.trident)) return;
        event.setCancelled(true);
        event.getEntity().remove();

        // grab our variables
        Player player = (Player) trident.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getCollidedWith();
        if (isValidAlly(player, victim)) {
            player.teleport(victim.getEyeLocation());
            final Vector velocity = player.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(0.5);
            player.setVelocity(velocity);
            return;
        }
        if (isValidEnemy(player, victim)) {

            // apply spell mechanics
            Location playerLoc = player.getLocation();
            Location targetLoc = victim.getLocation();

            Vector pushUpVector = new Vector(0.0D, 0.4D, 0.0D);
            victim.setVelocity(pushUpVector);

            final double xDir = (playerLoc.getX() - targetLoc.getX()) / 3.0D;
            double zDir = (playerLoc.getZ() - targetLoc.getZ()) / 3.0D;
            //final double hPower = 0.5D;

            DamageUtil.damageEntityPhysical(damage, victim, player, false, true, this);
            addStatusEffect(victim, RunicStatusEffect.SLOW_III, duration, false);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector pushVector = new Vector(xDir, 0.0D, zDir).normalize().multiply(2).setY(0.4D);
                    victim.setVelocity(pushVector);
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                }
            }.runTaskLater(RunicCore.getInstance(), 4L);
        }
    }

    public void setTridentSpeed(double tridentSpeed) {
        this.tridentSpeed = tridentSpeed;
    }
}

