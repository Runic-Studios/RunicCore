package com.runicrealms.plugin.spellapi.spells.runic.active;

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
public class Parry extends Spell {

    private static final double LAUNCH_PATH_MULT = 1.5;

    // constructor
    public Parry() {
        super("Parry", "You launch yourself backwards in the air!",
                ChatColor.WHITE, ClassEnum.RUNIC, 8, 15);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));

        // protect player from fall damage
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
