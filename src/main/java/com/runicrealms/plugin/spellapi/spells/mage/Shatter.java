package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.Map;
import java.util.UUID;

public class Shatter extends Spell {

    private static final double DURATION = 2.5;

    public Shatter() {
        super("Shatter",
                "Casting your &aArcane Bomb &7spell " +
                        "while you stand within the radius of your &aArcane Orb&7 " +
                        "empowers it, causing it to stun enemies " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof ArcaneBomb)) return;

        // location check
        Map<UUID, Location> arcaneOrbMap = ArcaneOrb.getArcaneOrbMap();
        if (arcaneOrbMap.get(event.getPlayer().getUniqueId()) == null) return;
        Location location = event.getPlayer().getLocation();
        Location orbLocation = arcaneOrbMap.get(event.getPlayer().getUniqueId());
        double dist = location.distanceSquared(orbLocation);

        if (dist <= ArcaneOrb.RADIUS * ArcaneOrb.RADIUS) {
            addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, DURATION, true);
        }
    }
}

