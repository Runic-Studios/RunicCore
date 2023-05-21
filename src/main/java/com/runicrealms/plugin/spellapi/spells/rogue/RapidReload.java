package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RapidReload extends Spell implements DurationSpell {
    private static final int MAX_ENEMIES = 3;
    private final Map<UUID, Integer> reloadingMap = new ConcurrentHashMap<>();
    private double duration;

    public RapidReload() {
        super("Rapid Reload", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Every enemy you hit with &aCannonfire &7reduces its cooldown by " + duration +
                "s. Capped at " + MAX_ENEMIES + " enemies hit.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onCannonfireHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof Cannonfire)) return;
        if (!reloadingMap.containsKey(event.getPlayer().getUniqueId())) {
            reloadingMap.put(event.getPlayer().getUniqueId(), 0);
        }
        if (reloadingMap.get(event.getPlayer().getUniqueId()) >= MAX_ENEMIES) {
            return;
        }
        reloadingMap.put(event.getPlayer().getUniqueId(),
                reloadingMap.get(event.getPlayer().getUniqueId()) + 1);
        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Cannonfire", duration);
    }

    /**
     * Reset the data map each cast
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFangsCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        reloadingMap.remove(event.getCaster().getUniqueId());
    }
}

