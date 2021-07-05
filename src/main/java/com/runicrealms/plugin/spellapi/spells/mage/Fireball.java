package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell implements MagicDamageSpell {

    private final boolean fireCone;
    private final boolean applyBurn;
    private final boolean iceBolt;
    private static final int DAMAGE_AMOUNT = 25;
    private static final double DAMAGE_PER_LEVEL = 2.75;
    private static final double FIREBALL_SPEED = 2;

    private SmallFireball fireball;
    private SmallFireball fireballLeft;
    private SmallFireball fireballRight;
    private Snowball snowball;

    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball " +
                        "that deals (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) spellʔ damage on impact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        fireCone = false;
        applyBurn = false;
        iceBolt = false;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        if (iceBolt) {
            snowball = player.launchProjectile(Snowball.class);
            final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
            snowball.setVelocity(velocity);
            snowball.setShooter(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
            EntityTrail.entityTrail(snowball, Particle.SNOWBALL);
            return;
        }
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        EntityTrail.entityTrail(fireball, Particle.FLAME);
        if (fireCone) {
            Vector left = rotateVectorAroundY(velocity, -22.5);
            Vector right = rotateVectorAroundY(velocity, 22.5);
            fireballLeft = player.launchProjectile(SmallFireball.class);
            fireballLeft.setIsIncendiary(false);
            fireballLeft.setVelocity(left);
            fireballLeft.setShooter(player);
            EntityTrail.entityTrail(fireballLeft, Particle.FLAME);
            fireballRight = player.launchProjectile(SmallFireball.class);
            fireballRight.setIsIncendiary(false);
            fireballRight.setVelocity(right);
            fireballRight.setShooter(player);
            EntityTrail.entityTrail(fireballRight, Particle.FLAME);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireballDamage(EntityDamageByEntityEvent e) {

        if (e.getDamager().equals(snowball)) {
            e.setCancelled(true);
            Player player = (Player) snowball.getShooter();
            if (player == null) return;
            if (!(e.getEntity() instanceof LivingEntity)) return;
            LivingEntity victim = (LivingEntity) e.getEntity();

            if (verifyEnemy(player, victim)) {
                DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, this);
                victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
            }
            return;
        }

        // only listen for our fireball (or icebolt)
        if (!e.getDamager().equals(fireball)
                && !e.getDamager().equals(fireballLeft)
                && !e.getDamager().equals(fireballRight)) return;

        e.setCancelled(true);

        // grab our variables
        Player player = (Player) fireball.getShooter();
        if (player == null) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) e.getEntity();

        if (verifyEnemy(player, victim)) {
//            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT + (DAMAGE_PER_LEVEL * player.getLevel()), victim, player, 100);
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, this);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);

            // scald
            if (hasPassive(player, "Scald")) {
                for (Entity en : fireball.getNearbyEntities(Scald.getRadius(), Scald.getRadius(), Scald.getRadius())) {
                    if (!verifyEnemy(player, en)) continue;
                    DamageUtil.damageEntitySpell(DAMAGE_AMOUNT * Scald.getDamagePercent(), (LivingEntity) en, player, this);
                }
            }

            if (applyBurn) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    DamageUtil.damageEntitySpell((DAMAGE_AMOUNT / 2), victim, player, this);
                    victim.getWorld().spawnParticle
                            (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                }, 20L);
            }
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

