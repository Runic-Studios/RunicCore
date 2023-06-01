package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import org.bukkit.entity.Player;

public class Damnation extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double radius;
    private double warmup;

    public Damnation() {
        super("Damnation", CharacterClass.WARRIOR);
        this.setDescription("You prime yourself with unholy magic, slowing yourself for " + warmup + "s. " +
                "Then, you consume all of your &7&osouls &7to get an aura around you " +
                "that lasts " + duration + "s! The aura’s radius and magic damage are equal to " +
                "Radius=(1 + souls x 1) and D=(5 + souls x 6). " +
                "Enemies within the aura are slowed and pulled " +
                "towards you each second while the aura persists!");
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

