package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;

import java.util.Map;

public class UmbralGrasp extends Spell implements DurationSpell, MagicDamageSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double speedMultiplier;

    public UmbralGrasp() {
        super("Umbral Grasp", CharacterClass.WARRIOR);
        this.setDescription("You conjure a spectral skull and launch it forwards! " +
                "Hitting an enemy deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage, " +
                "pulls you to the target, and slows them for " + duration + "s!");
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
        Number speedMultiplier = (Number) spellData.getOrDefault("speed-multiplier", 0);
        setSpeedMultiplier(speedMultiplier.doubleValue());
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
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

}

