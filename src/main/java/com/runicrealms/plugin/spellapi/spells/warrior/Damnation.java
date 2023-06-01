package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import org.bukkit.entity.Player;

import java.util.Map;

public class Damnation extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private double damage;
    private double damagePerLevel;
    private double damagePerSouls;
    private double duration;
    private double radius;
    private double radiusPerSouls;
    private double warmup;

    public Damnation() {
        super("Damnation", CharacterClass.WARRIOR);
        this.setDescription("You prime yourself with unholy magic, slowing yourself for " + warmup + "s. " +
                "Then, you consume all of your &f&osouls &7to get an aura around you " +
                "that lasts " + duration + "s! The aura’s radius is equal to " +
                "(" + radius + " + &f" + radiusPerSouls
                + "x&7 &f&osouls&7) and its magicʔ damage is (" + damage + " + &f" + damagePerSouls
                + "x&7 &f&osouls&7)! " +
                "Enemies within the aura are slowed and pulled " +
                "towards you each second while the aura persists!");
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
        Number radiusPerSouls = (Number) spellData.getOrDefault("radius-per-souls", 0);
        setRadiusPerSouls(radiusPerSouls.doubleValue());
    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
        Number magicDamagePerSouls = (Number) spellData.getOrDefault("magic-damage-per-souls", 0);
        setDamagePerSouls(magicDamagePerSouls.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

    }

    public void setDamagePerSouls(double damagePerSouls) {
        this.damagePerSouls = damagePerSouls;
    }

    public void setRadiusPerSouls(double radiusPerSouls) {
        this.radiusPerSouls = radiusPerSouls;
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

    @Override
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }
}

