package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell {

    private final boolean fireCone;
    private final boolean applyBurn;
    private final boolean iceBolt;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 25;
    private static final int CHILL_DURATION = 8;
    private static final int ROOT_DURATION = 3;
    private SmallFireball fireball;
    private SmallFireball fireballLeft;
    private SmallFireball fireballRight;
    private Snowball snowball;
    private HashSet<UUID> chilledPlayers;

    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        fireCone = false;
        applyBurn = false;
        iceBolt = false;
    }

    /**
     * Overriden method for tier set bonuses
     * @param fireCone 2-set bonus to apply a cone of projectiles
     * @param applyBurn 4-set bonus to apply burn effect
     */
    public Fireball(boolean fireCone, boolean applyBurn) {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        this.fireCone = fireCone;
        this.applyBurn = applyBurn;
        this.iceBolt = false;
    }

    /**
     * Overriden method for tier set bonuses
     * @param iceBolt swap spell
     */
    public Fireball(boolean iceBolt) {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        this.fireCone = false;
        this.applyBurn = false;
        this.iceBolt = iceBolt;
        this.chilledPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        if (iceBolt) {
            snowball = player.launchProjectile(Snowball.class);
            final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
            snowball.setVelocity(velocity);
            snowball.setShooter(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
            return;
        }
        fireball = player.launchProjectile(SmallFireball.class);
        fireball.setIsIncendiary(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
        fireball.setVelocity(velocity);
        fireball.setShooter(player);
        if (fireCone) {
            Vector left = rotateVectorAroundY(velocity, -22.5);
            Vector right = rotateVectorAroundY(velocity, 22.5);
            fireballLeft = player.launchProjectile(SmallFireball.class);
            fireballLeft.setIsIncendiary(false);
            fireballLeft.setVelocity(left);
            fireballLeft.setShooter(player);
            fireballRight = player.launchProjectile(SmallFireball.class);
            fireballRight.setIsIncendiary(false);
            fireballRight.setVelocity(right);
            fireballRight.setShooter(player);
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
                DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, 100);
                victim.getWorld().spawnParticle(Particle.SNOWBALL, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                if (!chilledPlayers.contains(victim.getUniqueId())) {
                    chilledPlayers.add(victim.getUniqueId());
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> chilledPlayers.remove(victim.getUniqueId()), CHILL_DURATION*20L);
                    Cone.coneEffect(victim, Particle.REDSTONE, CHILL_DURATION, 0, 20L, Color.AQUA);
                } else {
                    chilledPlayers.remove(victim.getUniqueId());
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ROOT_DURATION*20, 2));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ROOT_DURATION*20, 100000));
                }
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
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, 100);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);

            if (applyBurn) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    DamageUtil.damageEntitySpell((DAMAGE_AMOUNT/2), victim, player, 50);
                    victim.getWorld().spawnParticle
                            (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                        }, 20L);
            }
        }
    }
}

