package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

import java.util.Map;

public class Ruination extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double requiredSouls;
    private double percent;
    private double radius;

    public Ruination() {
        super("Ruination", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("After claiming " + requiredSouls + " souls, your next spell unleashes the spirits of your victims! " +
                "For the next " + duration + "s, the souls stream out of your body, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage per second " +
                "and lowering healing received by " +
                (percent * 100) + "% in a " + radius + " block cone in front of you!");
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
        Number requiredSouls = (Number) spellData.getOrDefault("required-souls", 0);
        setRequiredSouls(requiredSouls.doubleValue());
    }

    public void setRequiredSouls(double requiredSouls) {
        this.requiredSouls = requiredSouls;
    }

    public void setPercent(double percent) {
        this.percent = percent;
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
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

}
