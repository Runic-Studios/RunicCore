package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class RefreshingVolley extends Spell implements DurationSpell {
    private double duration;

    public RefreshingVolley() {
        super("Refreshing Volley", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("Each time you land a ranged attack on an enemy, " +
                "reduce the cooldown of your &aRemedy " +
                "&7spell by " + duration + "s!");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(RunicCore.getSpellAPI().isOnCooldown(event.getPlayer(), "Remedy"))) return;
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Remedy", duration);
    }
}

