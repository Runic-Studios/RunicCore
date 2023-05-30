package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

import java.util.Map;

public class TwilightResurgence extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double blindDuration;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double effectCooldown;
    private double radius;

    public TwilightResurgence() {
        super("Twilight Resurgence", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Each time a shield you apply is broken by damage, " +
                "reduce the cooldown of your &aCosmic Prism &7by " + duration + "s. " +
                "This effect has a " + effectCooldown + "s cooldown. " +
                "Additionally, release a pulse around the " +
                "player with the broken shield that " +
                "deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "in a " + radius + " block radius and blinds " +
                "enemies for " + blindDuration + "s!");
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number blindDuration = (Number) spellData.getOrDefault("blind-duration", 0);
        setBlindDuration(blindDuration.doubleValue());
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number effectCooldown = (Number) spellData.getOrDefault("effect-cooldown", 0);
        setEffectCooldown(effectCooldown.doubleValue());
    }

    public void setBlindDuration(double blindDuration) {
        this.blindDuration = blindDuration;
    }

    public void setEffectCooldown(double effectCooldown) {
        this.effectCooldown = effectCooldown;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
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

