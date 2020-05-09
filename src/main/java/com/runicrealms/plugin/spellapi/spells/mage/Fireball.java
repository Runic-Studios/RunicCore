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
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Fireball extends Spell {

    private boolean fireCone;
    private boolean applyBurn;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 25;
    private SmallFireball fireball;
    private SmallFireball fireballLeft;
    private SmallFireball fireballRight;

    public Fireball() {
        super ("Fireball",
                "You launch a projectile fireball" +
                        "\nthat deals " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 5, 15);
        fireCone = false;
        applyBurn = false;
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
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
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

        // only listen for our fireball
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                        () -> victim.getWorld().spawnParticle
                                (Particle.LAVA, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0), 20L);
            }
        }
    }
}

