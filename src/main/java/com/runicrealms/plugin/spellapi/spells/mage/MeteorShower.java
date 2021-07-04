package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class MeteorShower extends Spell {

    private final boolean fireCone;
    private final boolean applyBurn;
    private static final int AMOUNT = 4;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 35;
    private SmallFireball meteor;
    private SmallFireball meteorLeft;
    private SmallFireball meteorRight;
    private final HashMap<UUID, UUID> hasBeenHit;

    public MeteorShower() {
        super ("Meteor Shower",
                "You launch four projectile meteors " +
                        "that deal " + DAMAGE_AMOUNT + " spellʔ damage on " +
                        "impact!",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
        hasBeenHit = new HashMap<>();
        fireCone = false;
        applyBurn = false;
    }

    /**
     * Overriden method for tier set bonuses
     * @param fireCone 2-set bonus to apply a cone of projectiles
     * @param applyBurn 4-set bonus to apply burn effect
     */
    public MeteorShower(boolean fireCone, boolean applyBurn) {
        super ("Meteor Shower",
                "You launch four projectile meteors" +
                        "\nthat deal " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
        hasBeenHit = new HashMap<>();
        this.fireCone = fireCone;
        this.applyBurn = applyBurn;
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
                    meteor = player.launchProjectile(SmallFireball.class);
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
                        meteorLeft = player.launchProjectile(SmallFireball.class);
                        meteorLeft.setIsIncendiary(false);
                        meteorLeft.setVelocity(left);
                        meteorLeft.setShooter(player);
                        meteorRight = player.launchProjectile(SmallFireball.class);
                        meteorRight.setIsIncendiary(false);
                        meteorRight.setVelocity(right);
                        meteorRight.setShooter(player);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }

    private void createMeteorParticle(SmallFireball fireball) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMeteorHit(EntityDamageByEntityEvent e) {

        // only listen for our fireball
        if (!e.getDamager().equals(meteor)
                && !e.getDamager().equals(meteorLeft)
                && !e.getDamager().equals(meteorRight)) return;

        e.setDamage(0);
        e.setCancelled(true);

        // grab our variables
        Player player = (Player) meteor.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) e.getEntity();

        // prevent concussive hits
        if (hasBeenHit.get(player.getUniqueId()) == victim.getUniqueId()) return;

        if (verifyEnemy(player, victim)) {
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
                    DamageUtil.damageEntitySpell((DAMAGE_AMOUNT/2), victim, player, this);
                    victim.getWorld().spawnParticle
                            (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                }, 20L);
            }
        }
    }
}

