package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SuppressWarnings("FieldCanBeLocal")
public class Challenger extends Spell {

    private static final int DAMAGE_CAP = 35;
    private static final double HEALTH_MULT = .02;

    public Challenger() {
        super("Challenger",
                "Your weapon⚔ attacks also deal " + (int) (HEALTH_MULT * 100) +
                        "% of your enemy's max health! Capped at " + DAMAGE_CAP +
                        " versus monsters.",
                ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // fires AFTER other weapon events
    public void onDamage(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) e.getEntity();
        int bonus = (int) (le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * HEALTH_MULT);
        if (!(e.getEntity() instanceof Player) && bonus > DAMAGE_CAP)
            bonus = DAMAGE_CAP;
        e.setAmount(e.getAmount() + bonus);
        e.getPlayer().getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 15, 0.25f, 0.25f, 0.25f, 0);
        e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.1f, 2.0f);
    }
}

