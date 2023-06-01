package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

import java.util.Map;

public class Devour extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double percent;
    private double radius;

    public Devour() {
        super("Devour", CharacterClass.WARRIOR);
        this.setDescription("You cleave in front of you, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "to enemies within " + radius + " blocks and lowering their damage " +
                "dealt by " + (percent * 100) + "% for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

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

    public void setPercent(double percent) {
        this.percent = percent;
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

