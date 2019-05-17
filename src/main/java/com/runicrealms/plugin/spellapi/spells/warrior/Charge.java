package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Charge extends Spell {

    private static final double LAUNCH_PATH_MULT = 2.5;
    private static final double HEIGHT = 0.2;

    // constructor
    public Charge() {
        super("Charge", "You charge fearlessly into battle!",
                ChatColor.WHITE, 1, 5);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(look.getX(), HEIGHT, look.getZ()).normalize();

        // sounds, particles
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210,180,140), 20));

        // CHARGEE!!
        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));
    }
}
