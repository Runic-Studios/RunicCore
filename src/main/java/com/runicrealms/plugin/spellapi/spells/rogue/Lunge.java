package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Lunge extends Spell {

    private static final double HEIGHT = 1.2;

    public Lunge() {
        super("Lunge",
                "You lunge forward into the air, " +
                        "taking reduced fall damage on " +
                        "impact!",
                ChatColor.WHITE, ClassEnum.ROGUE, 8, 15);
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
