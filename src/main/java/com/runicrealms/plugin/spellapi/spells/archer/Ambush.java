package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Ambush extends Spell implements PhysicalDamageSpell {

    private static final int BLIND_DURATION = 2;
    private static final int COOLDOWN = 6;
    private static final int DAMAGE = 10;
    private static final int SPEED_DURATION = 3;
    private static final double DAMAGE_PER_LEVEL = 1.0;
    private static final double WARMUP = 2.0;
    private Set<UUID> ambushPlayers = new HashSet<>();

    public Ambush() {
        super("Ambush",
                "Sneaking without casting spells for at least " + (int) WARMUP +
                        "s causes your next ranged basic attack (if it lands) to ambush its target, " +
                        "dealing an additional (" +
                        DAMAGE + " + " + (int) DAMAGE_PER_LEVEL +
                        "x lvl) physical⚔ damage, blinding your opponent for " + BLIND_DURATION + "s, " +
                        "and granting you a a boost of speed for " +
                        SPEED_DURATION + "s! Cannot occur more than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        // todo: only add from the sneak
        ambushPlayers.add(event.getPlayer().getUniqueId());

    }
}

