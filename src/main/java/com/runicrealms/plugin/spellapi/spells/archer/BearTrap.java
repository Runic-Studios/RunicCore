package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;

public class BearTrap extends Spell {

    private static final double DURATION = 4;
    private final HashMap<UUID, HashMap<UUID, Integer>> ensnareMap;

    public BearTrap() {
        super("Bear Trap",
                "Damaging an enemy afflicted by &aHunter's Mark " +
                        "&73 times roots them for " + (int) DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
        ensnareMap = new HashMap<>();
    }

    private void applyRoot(Player damager, LivingEntity victim) {
        if (PowerShot.huntersMarkMap().get(damager.getUniqueId()) == null) {
            ensnareMap.remove(damager.getUniqueId()); // reset count if hunters mark wears off
            return;
        }
        if (!PowerShot.huntersMarkMap().get(damager.getUniqueId()).equals(victim.getUniqueId())) return;
        if (ensnareMap.get(damager.getUniqueId()) == null) {
            HashMap<UUID, Integer> victimCountMap = new HashMap<>();
            victimCountMap.put(victim.getUniqueId(), 1);
            ensnareMap.put(damager.getUniqueId(), victimCountMap);
        } else if (ensnareMap.get(damager.getUniqueId()).get(victim.getUniqueId()) < 2) {
            HashMap<UUID, Integer> victimCountMap = new HashMap<>();
            victimCountMap.put(victim.getUniqueId(), 2);
            ensnareMap.put(damager.getUniqueId(), victimCountMap);
        } else {
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
            Cone.coneEffect(victim, Particle.CRIT, DURATION, 0, 20L, Color.WHITE);
            addStatusEffect(victim, RunicStatusEffect.ROOT, DURATION);
            ensnareMap.remove(damager.getUniqueId());
        }
    }

    @EventHandler
    public void onRangedHit(PhysicalDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applyRoot(e.getPlayer(), (LivingEntity) e.getVictim());
    }

    @EventHandler
    public void onRangedHit(MagicDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applyRoot(e.getPlayer(), (LivingEntity) e.getVictim());
    }
}

