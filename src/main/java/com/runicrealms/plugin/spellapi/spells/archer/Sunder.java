package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Sunder extends Spell implements DurationSpell {
    private final Map<UUID, Long> sunderMap = new ConcurrentHashMap<>();
    private double duration;
    private double percent;

    public Sunder() {
        super("Sunder", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("Your spells now sunder the armor of enemies! " +
                "All enemies who take damage from &aPiercing Arrow " +
                "&7or &aRain Fire &7suffer " + (percent * 100) + "% additional " +
                "physical⚔ damage from all sources for the next " + duration + "s.");
        startMapTask();
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
        if (!sunderMap.containsKey(event.getPlayer().getUniqueId())) return;
        int additionalDamage = (int) (event.getAmount() * percent);
        event.setAmount(event.getAmount() + additionalDamage);
        event.getPlayer().playSound(event.getVictim().getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 2.0f);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof PiercingArrow || event.getSpell() instanceof RainFire))
            return;
        sunderMap.put(event.getCaster().getUniqueId(), System.currentTimeMillis());
    }

    private void startMapTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (sunderMap.isEmpty()) return;
            Iterator<UUID> iterator = sunderMap.keySet().iterator();
            while (iterator.hasNext()) {
                UUID uuid = iterator.next();
                long elapsedTime = System.currentTimeMillis() - sunderMap.get(uuid);
                if (elapsedTime > (duration * 1000))
                    sunderMap.remove(uuid);
            }
        }, 0, 1L);
    }

}
