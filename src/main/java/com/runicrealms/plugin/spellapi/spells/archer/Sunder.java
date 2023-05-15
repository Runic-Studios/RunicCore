package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;

public class Sunder extends Spell implements DurationSpell {
    private double duration;
    private double percent;

    public Sunder() {
        super("Sunder", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("Your spells now sunder the armor of enemies! " +
                "All enemies who take damage from &aPiercing Arrow " +
                "&7or &aRain of Arrows &7suffer " + (percent * 100) + "% additional " +
                "physical⚔ damage from all sources for the next " + duration + "s.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @EventHandler(priority = EventPriority.HIGH) // late
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isRanged()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof PiercingArrow || event.getSpell() instanceof RainFire))
            return;
        Bukkit.broadcastMessage("sunder");
    }

}
