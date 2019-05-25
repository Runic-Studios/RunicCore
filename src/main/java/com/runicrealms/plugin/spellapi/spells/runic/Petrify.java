package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixReverseParticleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.VertCircleFrame;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class Petrify extends Spell {

    // global variables
    private static int BUFF_DURATION = 7;
    private static int SPEED_AMPLIFIER = 2;
    private static final int BEAM_LENGTH = 8;

    // constructor
    public Petrify() {
        super("Petrify",
                "For " + BUFF_DURATION + " seconds, you gain a" +
                        "\nmassive boost to your speed!", ChatColor.WHITE,1, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // sound effects
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SKELETON_DEATH, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);

        // create three beams
        Vector middle = pl.getEyeLocation().getDirection().normalize();
        Vector left = rotateVectorAroundY(middle, -15.0);
        Vector right = rotateVectorAroundY(middle, 15.0);

        // begin particle effect & entity check tasks
        startTask(pl, new Vector[]{middle, left, right});
    }

    // particles, vectors
    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Location location = player.getEyeLocation();
            for (double t = 0; t < BEAM_LENGTH; t += 1) {
                location.add(vector);
                player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.BLACK, 1));
                player.getWorld().spawnParticle(Particle.REDSTONE, location, 5, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.GREEN, 1));
                if (location.getBlock().getType().isSolid()) {
                    break;
                }
            }
        }
    }
}

