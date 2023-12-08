package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * New ultimate passive for berserker
 *
 * @author BoBoBalloon
 */
public class Bloodbath extends Spell implements AttributeSpell {
    private static final long EXPIRY_DURATION = 5; // 5 seconds
    private final Map<UUID, Long> eventMap = new HashMap<>(); // Prevents multiple benefits per cast
    private double percent;
    private double healthCeiling;
    private double multiplier;
    private double baseValue;
    private String statName;

    public Bloodbath() {
        super("Bloodbath", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("Hitting an enemy with &aCleave&7 heals✦ you for " +
                "(" + this.baseValue + " + &f" + this.multiplier + "x &e" + prefix + "&7) health! " +
                "Additionally you do " + (this.percent * 100) + "% more damage to &cbleeding &7enemies " +
                "that are below " + (this.healthCeiling * 100) + "% of their max health. " +
                "Cannot occur more than once per cast.");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number percent = (Number) spellData.getOrDefault("percent", 0.1);
        this.percent = percent.doubleValue();
        Number healthCeiling = (Number) spellData.getOrDefault("health-ceiling", 0.5);
        this.healthCeiling = healthCeiling.doubleValue();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Cleave)) return;

        eventMap.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > EXPIRY_DURATION); // Clean up old entries

        if (eventMap.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        eventMap.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());

        double percentHealth = RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), this.getStatName()) * this.multiplier;
        this.healPlayer(event.getPlayer(), event.getPlayer(), baseValue + percentHealth);

        if (!this.hasSpellEffect(event.getVictim().getUniqueId(), SpellEffectType.BLEED)) return;
        double healthRatio = event.getVictim().getHealth() / event.getVictim().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (healthRatio >= this.healthCeiling) return;
        event.setAmount((int) (event.getAmount() * (1 + this.percent)));
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
}
