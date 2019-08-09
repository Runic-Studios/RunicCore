package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Envenom extends Spell {

    private static final int DURATION = 5;
    private static final double PERCENT = 100;
    private List<UUID> venomers = new ArrayList<>();

    public Envenom() {
        super("Envenom",
                "For " + DURATION + " seconds, you coat your blade" +
                        "\nin a deadly venom, causing your weapon⚔" +
                        "\nattacks to deal twice the damage for" +
                        "\nthe duration!",
                ChatColor.WHITE,15, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        venomers.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 0.5f);
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.GREEN);

        new BukkitRunnable() {
            @Override
            public void run() {
                venomers.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Damage nearby entities
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!venomers.contains(e.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        e.setAmount(e.getAmount() + extraAmt);
        Entity victim = e.getEntity();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.0f);
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                10, 0.5F, 0.5F, 0.5F, 0, new Particle.DustOptions(Color.GREEN, 20));
    }
}

