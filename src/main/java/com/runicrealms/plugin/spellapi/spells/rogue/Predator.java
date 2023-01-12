package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.UUID;

public class Predator extends Spell {

    private static final double DURATION = 2.5;
    private static final HashSet<UUID> predators = new HashSet<>();

    public Predator() {
        super("Predator",
                "Upon reappearing after becoming invisible, " +
                        "your next melee attack will stun your target for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    public static HashSet<UUID> getPredators() {
        return predators;
    }

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onPredatorHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!predators.contains(event.getPlayer().getUniqueId())) return;
        predators.remove(event.getPlayer().getUniqueId());
        addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, DURATION);
    }
}

