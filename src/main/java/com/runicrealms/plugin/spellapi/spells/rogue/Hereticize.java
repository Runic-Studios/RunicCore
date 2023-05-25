package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

public class Hereticize extends Spell implements DurationSpell, MagicDamageSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;

    public Hereticize() {
        super("Hereticize", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Anytime a &7&obranded &7enemy uses an ability, " +
                "all of your active ability cooldowns are reduced by " + duration + "s! " +
                "&7&oBranded &7enemies take an additional " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage on " +
                "hit from all sources!");
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
}
