package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class SurvivalInstinct extends Spell {

    private static final int COOLDOWN = 60;
    private static final int DURATION = 6;
    private static final double PERCENT_THRESHOLD = .25;
    private final HashSet<UUID> cooldown;
    private final HashSet<UUID> shielders;

    public SurvivalInstinct() {
        super("Survival Instinct",
                "Upon taking damage that would reduce you below " +
                        (int) (PERCENT_THRESHOLD * 100) + "% health, you " +
                        "gain a shield which negates all mob and physical⚔ damage " +
                        "for " + DURATION + "s! Cannot occur more than once " +
                        "every " + COOLDOWN + "s.",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
        cooldown = new HashSet<>();
        shielders = new HashSet<>();
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        Player victim = (Player) e.getVictim();
        if (!hasPassive(victim.getUniqueId(), this.getName())) return;
        if (shielders.contains(victim.getUniqueId())) {
            e.setCancelled(true);
        } else if (victim.getHealth() - e.getAmount() <= (victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * PERCENT_THRESHOLD)
                && !cooldown.contains(victim.getUniqueId())) {
            applyShield(victim);
        }
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        Player victim = (Player) e.getVictim();
        if (!hasPassive(victim.getUniqueId(), this.getName())) return;
        if (shielders.contains(victim.getUniqueId())) {
            e.setCancelled(true);
        } else if (victim.getHealth() - e.getAmount() <= (victim.getHealth() * PERCENT_THRESHOLD)
                && !cooldown.contains(victim.getUniqueId())) {
            applyShield(victim);
        }
    }

    private void applyShield(Player victim) {
        cooldown.add(victim.getUniqueId());
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, () -> cooldown.remove(victim.getUniqueId()), COOLDOWN * 20L);
        shielders.add(victim.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> shielders.remove(victim.getUniqueId()), DURATION * 20L);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1.0f);
        Cone.coneEffect(victim, Particle.CRIT, DURATION, 0, 20L, Color.WHITE);
    }
}
