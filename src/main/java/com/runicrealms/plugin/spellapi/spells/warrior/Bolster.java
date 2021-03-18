package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Bolster extends Spell {

    private static final int DURATION = 5;
    private static final double PERCENT = .25;
    private static final int RADIUS = 10;
    private final HashSet<UUID> bolsteredPlayers;

    public Bolster() {
        super("Bolster",
                "Your allies rally to you! Yourself and allies within " +
                        RADIUS + " blocks are bolstered, receiving a " +
                        (int) (PERCENT * 100) + "% damage reduction buff " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 15, 20);
        bolsteredPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, pl.getLocation(), 50, 0.5f, 0.5f, 0.5f, 0);
        bolsteredPlayers.add(pl.getUniqueId());
        Cone.coneEffect(pl, Particle.REDSTONE, DURATION, 0, 20L, Color.WHITE);
        for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!verifyAlly(pl, en)) continue;
            LivingEntity le = (LivingEntity) en;
            bolsteredPlayers.add(en.getUniqueId());
            // sounds ?
            Cone.coneEffect(le, Particle.REDSTONE, DURATION, 0, 20L, Color.WHITE);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, bolsteredPlayers::clear, DURATION * 20L);
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!bolsteredPlayers.contains(e.getVictim().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT)));
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!bolsteredPlayers.contains(e.getEntity().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT)));
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!bolsteredPlayers.contains(e.getEntity().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() * (1 - PERCENT)));
    }
}
