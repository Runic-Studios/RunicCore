package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Predator extends Spell {

    private static final int DURATION = 3;
    private static final double PERCENT = 0.75;
    private static final HashSet<UUID> predators = new HashSet<>();

    public Predator() {
        super ("Predator",
                "Upon reappearing after becoming invisible, " +
                        "you gain a " + (int) (PERCENT * 100) + "% damage " +
                        "buff for " + DURATION + "s! ",
                ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs last
    public void onPredatorHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!predators.contains(e.getPlayer().getUniqueId())) return;
        e.setAmount((int) predatorDamage(e.getPlayer(), e.getEntity(), e.getAmount()));
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs last
    public void onPredatorHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!predators.contains(e.getPlayer().getUniqueId())) return;
        e.setAmount((int) predatorDamage(e.getPlayer(), e.getEntity(), e.getAmount()));
    }

    private double predatorDamage(Player pl, Entity en, double eventAmount) {
        LivingEntity victim = (LivingEntity) en;
        pl.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.BLACK, 1));
        return eventAmount + (eventAmount * PERCENT);
    }

    public static int getDuration() {
        return DURATION;
    }

    public static HashSet<UUID> getPredators() {
        return predators;
    }
}

