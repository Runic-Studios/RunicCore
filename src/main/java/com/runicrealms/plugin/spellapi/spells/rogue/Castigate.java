package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;

import java.util.Map;

public class Castigate extends Spell implements DurationSpell, MagicDamageSpell {
    private double damage;
    private double damagePerLevel;
    private double durationToHit;
    private double numberOfTicks;
    private double percent;

    public Castigate() {
        super("Castigate", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("After casting a spell, your next attack " +
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
// todo:
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
