package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;

import java.util.Map;

public class SoulReaper extends Spell implements DurationSpell {
    private double duration;
    private double maxStacks;
    private double percent;

    public SoulReaper() {
        super("Soul Reaper", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Landing your &aDevour &7or &aUmbral Grasp &7spell " +
                "on enemies builds up &7&osouls&7. " +
                "For each stack of souls you have, " +
                "you take " + (percent * 100) + "% less damage! " +
                "Your souls last " + duration + "s and have their duration reset when stacked. " +
                "Max " + maxStacks + " souls.");
    }


    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 0);
        setMaxStacks(maxStacks.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

}
