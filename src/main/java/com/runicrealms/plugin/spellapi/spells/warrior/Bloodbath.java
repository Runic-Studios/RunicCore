package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;

/**
 * New ultimate passive for berserker
 *
 * @author BoBoBalloon
 */
public class Bloodbath extends Spell implements HealingSpell {
    private double healing;
    private double healingPerStrength;
    private double percent;
    private double healthCeiling;

    public Bloodbath() {
        super("Bloodbath", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Hitting an enemy with &aCleave&7 heals you for (" + this.healing + " + &f" + this.healingPerStrength + "x &eSTR&7)% of your missing HP. " +
                "Additionally you do " + (this.percent * 100) + "% more damage to bleeding enemies that are below " + (this.healthCeiling * 100) + "% max HP.");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number percent = (Number) spellData.getOrDefault("percent", 0.1);
        this.percent = percent.doubleValue();
        Number healthCeiling = (Number) spellData.getOrDefault("health-ceiling", 0.5);
        this.healthCeiling = healthCeiling.doubleValue();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !(event.getSpell() instanceof Cleave)) {
            return;
        }

        double percent = RunicCore.getStatAPI().getPlayerStrength(event.getPlayer().getUniqueId()) * this.healingPerStrength;
        double heal = event.getPlayer().getHealth() * (percent / 100);

        this.healPlayer(event.getPlayer(), event.getPlayer(), heal);

        if (!this.hasStatusEffect(event.getVictim().getUniqueId(), RunicStatusEffect.BLEED)) {
            return;
        }

        double healthRatio = event.getVictim().getHealth() / event.getVictim().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (healthRatio >= this.healthCeiling) {
            return;
        }

        event.setAmount((int) (event.getAmount() * (1 + this.percent)));
    }

    @Override
    public double getHeal() {
        return this.healing;
    }

    @Override
    public void setHeal(double heal) {
        this.healing = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return this.healingPerStrength;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerStrength = healingPerLevel;
    }
}
