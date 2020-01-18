package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Slam extends Spell {

    private static final double KNOCKUP_AMT = 0.2;
    private static final int DAMAGE_AMT = 6;
    private static final double HEIGHT = 1.2;
    private static final int RADIUS = 5;

    public Slam() {
        super("Slam", "You charge fearlessly into the air!" +
                        "\nUpon hitting the ground, you deal " +
                        "\n" + DAMAGE_AMT + " spellʔ damage to enemies within" +
                        "\n" + RADIUS + " blocks and knock them up!",
                ChatColor.WHITE, 8, 15);
    }

//    @Override
//    public boolean attemptToExecute(Player pl) {
//        if (!pl.isOnGround()) {
//            pl.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
//            return false;
//        }
//        return true;
//    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sounds, particles
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);

        // apply effects
        final Vector velocity = pl.getVelocity().setY(HEIGHT);
        Vector directionVector = pl.getLocation().getDirection();
        directionVector.setY(0);
        directionVector.normalize();

        float pitch = pl.getEyeLocation().getPitch();
        if (pitch > 0.0F) {
            pitch = -pitch;
        }

        float multiplier = (90.0F + pitch) / 50.0F;
        directionVector.multiply(multiplier);
        velocity.add(directionVector);
        velocity.multiply(new Vector(0.6D, 0.8D, 0.6D));

        pl.setVelocity(velocity);

        new BukkitRunnable() {
            @Override
            public void run() {
                pl.setVelocity(new Vector
                        (pl.getLocation().getDirection().getX(), -10.0,
                                pl.getLocation().getDirection().getZ()).multiply(2).normalize());
                pl.setFallDistance(-512.0F);
            }
        }.runTaskLater(RunicCore.getInstance(), 20L);

        // todo: fix potential memory leak if player falls into void or never hits ground?
        new BukkitRunnable() {
            @Override
            public void run() {

                if (pl.isOnGround() || pl.getFallDistance() == 1) {

                    this.cancel();
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
                    pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
                    pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                            25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

                    for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (verifyEnemy(pl, en)) {
                            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, pl, false);
                            Vector force = (pl.getLocation().toVector().subtract
                                    (en.getLocation().toVector()).multiply(0).setY(KNOCKUP_AMT));
                            en.setVelocity(force.normalize());
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }
}
