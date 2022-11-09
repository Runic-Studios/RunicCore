package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class MeteorShower extends Spell implements MagicDamageSpell {

    private static final int AMOUNT = 4;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 35;
    private static final double DAMAGE_PER_LEVEL = 0.85;
    private final boolean fireCone;
    private final boolean applyBurn;
    private final HashMap<UUID, UUID> hasBeenHit;
    private LargeFireball meteor;
    private LargeFireball meteorLeft;
    private LargeFireball meteorRight;

    public MeteorShower() {
        super("Meteor Shower",
                "You launch four projectile meteors " +
                        "that deal (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage on impact!",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
        hasBeenHit = new HashMap<>();
        fireCone = false;
        applyBurn = false;
    }

    private void createMeteorParticle(LargeFireball fireball) {
        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fireball.isDead()) {
                    this.cancel();
                }
                fireball.getWorld().spawnParticle(Particle.LAVA, fireball.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= AMOUNT) {
                    this.cancel();
                } else {
                    count += 1;
                    meteor = player.launchProjectile(LargeFireball.class);
                    createMeteorParticle(meteor);
                    meteor.setIsIncendiary(false);
                    meteor.setYield(0F);
                    final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
                    meteor.setVelocity(velocity);
                    meteor.setShooter(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                    if (fireCone) {
                        Vector left = rotateVectorAroundY(velocity, -22.5);
                        Vector right = rotateVectorAroundY(velocity, 22.5);
                        meteorLeft = player.launchProjectile(LargeFireball.class);
                        meteorLeft.setIsIncendiary(false);
                        meteorLeft.setYield(0F);
                        meteorLeft.setIsIncendiary(false);
                        meteorLeft.setVelocity(left);
                        meteorLeft.setShooter(player);
                        meteorRight = player.launchProjectile(LargeFireball.class);
                        meteorRight.setIsIncendiary(false);
                        meteorRight.setYield(0F);
                        meteorRight.setIsIncendiary(false);
                        meteorRight.setVelocity(right);
                        meteorRight.setShooter(player);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof LargeFireball) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMeteorHit(EntityDamageByEntityEvent event) {

        // only listen for our fireball
        if (!event.getDamager().equals(meteor)
                && !event.getDamager().equals(meteorLeft)
                && !event.getDamager().equals(meteorRight)) return;

        event.setDamage(0);
        event.setCancelled(true);

        // grab our variables
        Player player = (Player) meteor.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // prevent concussive hits
        if (hasBeenHit.get(player.getUniqueId()) == victim.getUniqueId()) return;

        if (isValidEnemy(player, victim)) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, this);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            hasBeenHit.put(player.getUniqueId(), victim.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    hasBeenHit.clear();
                }
            }.runTaskLaterAsynchronously(RunicCore.getInstance(), 10L);

            if (applyBurn) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    DamageUtil.damageEntitySpell((DAMAGE_AMOUNT / 2.0), victim, player, this);
                    victim.getWorld().spawnParticle
                            (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                }, 20L);
            }
        }
    }
}

