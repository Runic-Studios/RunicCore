package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Enflame extends Spell {

    private static final int DURATION = 5;
    private static final double PERCENT = 100;
    private List<UUID> flamers = new ArrayList<>();

    public Enflame() {
        super("Enflame",
                "For " + DURATION + " seconds, you ignite your blade" +
                        "\nwith pure flame, causing your weapon⚔" +
                        "\nattacks to deal twice the damage for" +
                        "\nthe duration!",
                ChatColor.WHITE, ClassEnum.ROGUE, 15, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        flamers.add(pl.getUniqueId());
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
        Cone.coneEffect(pl, Particle.FLAME, DURATION, 0, 20L, Color.GREEN);

        new BukkitRunnable() {
            @Override
            public void run() {
                flamers.remove(pl.getUniqueId());
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20L);
    }

    /**
     * Damage nearby entities
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!flamers.contains(e.getPlayer().getUniqueId())) return;
        double percent = PERCENT / 100;
        int extraAmt = (int) (e.getAmount() * percent);
        e.setAmount(e.getAmount() + extraAmt);
        Entity victim = e.getEntity();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.0f);
        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.FLAME, victim.getLocation(), 10, 0.5F, 0.5F, 0.5F, 0);
    }
}

