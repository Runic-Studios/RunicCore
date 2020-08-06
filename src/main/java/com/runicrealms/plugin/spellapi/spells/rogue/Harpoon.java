package com.runicrealms.plugin.spellapi.spells.rogue;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Harpoon extends Spell {

    private static final double TRIDENT_SPEED = 1.25;
    private Trident trident;

    public Harpoon() {
        super ("Harpoon",
                "You launch a projectile harpoon" +
                        "\nwhich pulls your enemy towards" +
                        "\nyou!",
                ChatColor.WHITE, ClassEnum.ROGUE, 12, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.swingMainHand();
        trident = player.launchProjectile(Trident.class);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(TRIDENT_SPEED);
        trident.setDamage(0);
        trident.setVelocity(velocity);
        trident.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.5f);

        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (trident.isDead())
                    this.cancel();
                trident.getWorld().spawnParticle(Particle.REDSTONE, trident.getLocation(),
                        10, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onTridentDamage(ProjectileCollideEvent e) {

        if (!e.getEntity().equals(this.trident)) return;
        e.setCancelled(true);
        e.getEntity().remove();

        // grab our variables
        Player player = (Player) trident.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) e.getCollidedWith();
        if (!verifyEnemy(player, victim)) return;

        // apply spell mechanics
        Location playerLoc = player.getLocation();
        Location targetLoc = victim.getLocation();

        Vector pushUpVector = new Vector(0.0D, 0.4D, 0.0D);
        victim.setVelocity(pushUpVector);

        final double xDir = (playerLoc.getX() - targetLoc.getX()) / 3.0D;
        double zDir = (playerLoc.getZ() - targetLoc.getZ()) / 3.0D;
        //final double hPower = 0.5D;

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

