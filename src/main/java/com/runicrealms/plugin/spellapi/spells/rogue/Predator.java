package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Predator extends Spell implements DurationSpell {
    private static final Map<UUID, Set<UUID>> predators = new ConcurrentHashMap<>();
    private double duration;

    public Predator() {
        super("Predator", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("When you basic-attack enemies who have been hit by " +
                "&aTwin Fangs&7, lower the cooldown of &aTwin Fangs &7by " + duration +
                "s! Affected enemies are reset each cast.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Reset the data map each cast
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFangsCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        predators.remove(event.getCaster().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onPredatorHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;

        // Reduce fangs CD if the player hits a basic attack
        if (event.isBasicAttack()) {
            if (!predators.containsKey(event.getPlayer().getUniqueId())) return;
            if (!predators.get(event.getPlayer().getUniqueId()).contains(event.getVictim().getUniqueId()))
                return;
            RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Twin Fangs", duration);

            // Add victim to list of marked enemies
        } else if (event.getSpell() != null && event.getSpell() instanceof TwinFangs) {
            if (!predators.containsKey(event.getPlayer().getUniqueId())) {
                predators.put(event.getPlayer().getUniqueId(), new HashSet<>());
            }
            predators.get(event.getPlayer().getUniqueId()).add(event.getVictim().getUniqueId());
        }
    }
}

