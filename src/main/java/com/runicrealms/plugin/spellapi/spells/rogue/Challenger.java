package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SuppressWarnings("FieldCanBeLocal")
public class Challenger extends Spell {
    private static final int DAMAGE_CAP = 35;
    private static final double HEALTH_MULT = .03;

    public Challenger() {
        super("Challenger", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Your basic attacks also deal " + (int) (HEALTH_MULT * 100) +
                "% of your enemy's max health! Capped at " + DAMAGE_CAP +
                " versus monsters.");
    }

    @EventHandler(priority = EventPriority.HIGHEST) // fires AFTER other weapon events
    public void onDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        LivingEntity le = event.getVictim();
        int bonus = (int) (le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * HEALTH_MULT);
        if (!(event.getVictim() instanceof Player) && bonus > DAMAGE_CAP)
            bonus = DAMAGE_CAP;
        event.setAmount(event.getAmount() + bonus);
        event.getPlayer().getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 15, 0.25f, 0.25f, 0.25f, 0);
    }
}

