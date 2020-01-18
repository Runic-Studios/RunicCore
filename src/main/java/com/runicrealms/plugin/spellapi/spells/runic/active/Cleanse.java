package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Cleanse extends Spell {

    public Cleanse() {
        super("Cleanse",
                "You cleanse negative effects" +
                        "\non you, freeing you from" +
                        "\nblindness and slows!",
                ChatColor.WHITE, 6, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0F, 2.0F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
        pl.setGlowing(true);
        for(PotionEffect effect : pl.getActivePotionEffects()) {
            pl.removePotionEffect(effect.getType());
        }
        Cone.coneEffect(pl, Particle.REDSTONE, 2, 0, 20L, Color.WHITE);
        Cone.coneEffect(pl, Particle.SPELL_INSTANT, 2, 0, 20L, Color.WHITE);
        new BukkitRunnable() {
            @Override
            public void run() {
                pl.setGlowing(false);
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 40L);
    }
}

