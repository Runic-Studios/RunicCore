package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Lunge extends Spell {

    private static final double HEIGHT = 1.2;

    public Lunge() {
        super("Lunge",
                "You lunge forward into the air," +
                        "\ntaking reduced fall damage on" +
                        "\nimpact!", ChatColor.WHITE,8, 12);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

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
        velocity.multiply(new Vector(0.7D, 0.7D, 0.7D));

        pl.setVelocity(velocity);

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.8f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 10));

        new BukkitRunnable() {
            @Override
            public void run() {

                if (pl.isOnGround()) {
                    this.cancel();
                } else {
                    pl.setFallDistance(-8.0F);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 1L);
    }
}
