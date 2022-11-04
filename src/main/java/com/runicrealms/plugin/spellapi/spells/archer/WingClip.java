package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class WingClip extends Spell {

    private static final double DURATION = 1.5;
    private final HashSet<UUID> wingClippers;

    public WingClip() {
        super("Wing Clip",
                "After casting your &aGrapple &7spell, " +
                        "your first ranged basic attack will " +
                        "root its enemy for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.ARCHER, 0, 0);
        this.setIsPassive(true);
        wingClippers = new HashSet<>();
    }

    @EventHandler
    public void onGrappleCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Grapple)) return;
        wingClippers.add(event.getCaster().getUniqueId());
    }

    @EventHandler
    public void onRangedHit(PhysicalDamageEvent event) {
        if (!event.isRanged()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(wingClippers.contains(event.getPlayer().getUniqueId()))) return;
        wingClippers.remove(event.getPlayer().getUniqueId());
        Entity en = event.getVictim();
        addStatusEffect(en, EffectEnum.ROOT, DURATION);
        en.getWorld().spawnParticle(Particle.REDSTONE, en.getLocation(), 15, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
    }
}

