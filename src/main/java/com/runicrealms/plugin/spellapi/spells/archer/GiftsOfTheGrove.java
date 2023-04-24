package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.runicitems.Stat;
import org.bukkit.event.EventHandler;

import java.util.Map;

public class GiftsOfTheGrove extends Spell implements AttributeSpell {
    private double baseValue;
    private double multiplier;
    private double percent;
    private String statName;

    public GiftsOfTheGrove() {
        super("Gifts Of The Grove", CharacterClass.ARCHER);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String statName = stat != null ? stat.getPrefix() : "";
        this.setDescription("While inside your &aSacred Grove&7, " +
                "your healing is increased by (" + baseValue + " + " + multiplier + "x " + statName + ")%! " +
                "If you are inside the grove when it expires, " +
                "it releases one more pulse, " +
                "healing allies for " + percent + "% of its normal value!");
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

    public void loadAttributeData(Map<String, Object> spellData) {
        setStatName((String) spellData.getOrDefault("attribute", ""));
        Number baseValue = (Number) spellData.getOrDefault("attribute-base-value", 0);
        setBaseValue(baseValue.doubleValue());
        Number multiplier = (Number) spellData.getOrDefault("attribute-multiplier", 0);
        setMultiplier(multiplier.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue() / 100);
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        // get grove location
        // do a distanceSquared check
        // increase heal
        // check when grove expires? then heal again. needs a mini event?
    }
}
