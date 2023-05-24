package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class Scurvy extends Spell implements DurationSpell {
    private double duration;
    private double hungerBars;
    private double percent;

    public Scurvy() {
        super("Scurvy", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("Your empowered attack from &aSprint &7is laden with disease! " +
                "The target hit receives nausea for the next " + duration + "s " +
                "and loses " + hungerBars + " hunger bars! " +
                "Against mobs this increases the damage by " + (percent * 100) + "% instead.");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEmpoweredSprint(Sprint.EmpoweredSprintEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        event.getVictim().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) duration * 20, 999));
        // Players lose hunger bars
        if (event.getVictim() instanceof Player playerVictim) {
            playerVictim.setFoodLevel(playerVictim.getFoodLevel() - (int) hungerBars);
        } else {
            event.setDamage(event.getDamage() + (event.getDamage() * percent));
        }
    }

    public void setHungerBars(double hungerBars) {
        this.hungerBars = hungerBars;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number hungerBars = (Number) spellData.getOrDefault("hunger-bars", 0);
        setHungerBars(hungerBars.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

}

