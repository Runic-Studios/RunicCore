package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Hexagon;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;

public class CosmicPrism extends Spell implements AttributeSpell, DurationSpell, RadiusSpell, ShieldingSpell {
    private double baseValue;
    private double buffDuration;
    private double duration;
    private double multiplier;
    private double period;
    private double radius;
    private double shield;
    private double shieldPerLevel;
    private String statName;

    public CosmicPrism() {
        super("Cosmic Prism", CharacterClass.CLERIC);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("You summon a prism of starlight that illuminates " +
                "the ground in a " + radius + " block radius for the next " + duration + "s. " +
                "Allies standing in the light receive a " +
                "stacking (" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) shield every " + period + "s! " +
                "The final tick of the prism applies a buff to allies, " +
                "reducing their incoming damage by (" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)% " +
                "for the next " + buffDuration + "s!");
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number buffDuration = (Number) spellData.getOrDefault("buff-duration", 0);
        setBuffDuration(buffDuration.doubleValue());
        Number period = (Number) spellData.getOrDefault("period", 0);
        setPeriod(period.doubleValue());
    }

    private void setPeriod(double period) {
        this.period = period;
    }

    private void setBuffDuration(double buffDuration) {
        this.buffDuration = buffDuration;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        new Hexagon(castLocation, duration, radius).runTaskTimer(RunicCore.getInstance(), 0, 20L);
        // todo: everything else
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

}

