package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Castigate extends Spell implements DurationSpell, MagicDamageSpell {
    private final Set<UUID> buffedPlayersSet = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double durationToHit;
    private double numberOfTicks;
    private double percent;

    public Castigate() {
        super("Castigate", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("After casting a spell, your next basic attack " +
                "within " + durationToHit + "s now burns the target " +
                "for (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "per second for " + numberOfTicks + "s. During this time, " +
                "the target receives " + (percent * 100) + "% less healing " +
                "from all sources!");
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getNumberOfTicks() {
        return numberOfTicks;
    }

    public void setNumberOfTicks(double numberOfTicks) {
        this.numberOfTicks = numberOfTicks;
    }

    public double getDurationToHit() {
        return durationToHit;
    }

    public void setDurationToHit(double durationToHit) {
        this.durationToHit = durationToHit;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number durationToHit = (Number) spellData.getOrDefault("duration-to-hit", 0);
        setDurationToHit(durationToHit.doubleValue());
        Number numberOfTicks = (Number) spellData.getOrDefault("number-of-ticks", 0);
        setNumberOfTicks(numberOfTicks.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    @Override
    public double getDuration() {
        return durationToHit;
    }

    @Override
    public void setDuration(double duration) {
        this.durationToHit = duration;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        buffedPlayersSet.add(event.getCaster().getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> buffedPlayersSet.remove(event.getCaster().getUniqueId()), (long) durationToHit * 20L);
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!buffedPlayersSet.contains(event.getPlayer().getUniqueId())) return;
        applyCastigation(event.getPlayer(), event.getVictim());
    }

    private void applyCastigation(Player caster, LivingEntity victim) {
        Spell spell = this;
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {

                count += 1;
                if (count > numberOfTicks)
                    this.cancel();
                else {
                    Bukkit.broadcastMessage("test");
                    DamageUtil.damageEntitySpell(damage, victim, caster, spell);
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }
}
