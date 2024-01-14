package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.HolyFervorEffect;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Map;

public class SacredWings extends Spell implements DurationSpell, RadiusSpell, ShieldingSpell {
    private double allyShield;
    private double allyShieldPerLevel;
    private double duration;
    private double knockback;
    private double radius;
    private double shield;
    private double shieldPerLevel;
    private double sweepDuration;

    public SacredWings() {
        super("Sacred Wings", CharacterClass.WARRIOR);
        this.setDescription("For the next " + duration + "s, you conjure wings of light, " +
                "empowering you with &6holy fervor&7!" +
                "\n\n&2&lEFFECT &6Holy Fervor" +
                "\n&7You gain a boost of speed " +
                "and a (" + shield + " + &f" + shieldPerLevel + "x&7 lvl) health shield! " +
                "While &6holy fervor &7lasts, your basic attacks " +
                "transform into radiant sweeps of magic, launching enemies back and " +
                "&eshielding &7allies within " + radius + " blocks for " +
                "(" + allyShield + " + &f" + allyShieldPerLevel + "x&7 lvl) health! " +
                "Cannot sweep the same target more than once every " + sweepDuration + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // give spell effect
        new HolyFervorEffect(player, this.duration).initialize();
        this.addStatusEffect(player, RunicStatusEffect.SPEED_I, this.duration, false);
        this.shieldPlayer(player, player, this.shield, this);
        // entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.01F, 0.5F);
    }

    @EventHandler
    public void onBasicAttack(BasicAttackEvent event) {
        if (!this.hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.HOLY_FERVOR)) return;
        sweepEffect();
    }

    private void sweepEffect() {

    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number allyShield = (Number) spellData.getOrDefault("ally-shield", 20);
        setAllyShield(allyShield.doubleValue());
        Number allyShieldPerLevel = (Number) spellData.getOrDefault("ally-shield-per-level", 1.0);
        setAllyShieldPerLevel(allyShieldPerLevel.doubleValue());
        Number knockback = (Number) spellData.getOrDefault("knockback", 1.0);
        setKnockback(knockback.doubleValue());
        Number sweepDuration = (Number) spellData.getOrDefault("sweep-duration", 8);
        setSweepDuration(sweepDuration.doubleValue());
    }

    public void setSweepDuration(double sweepDuration) {
        this.sweepDuration = sweepDuration;
    }

    public void setAllyShield(double allyShield) {
        this.allyShield = allyShield;
    }

    public void setAllyShieldPerLevel(double allyShieldPerLevel) {
        this.allyShieldPerLevel = allyShieldPerLevel;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setKnockback(double knockback) {
        this.knockback = knockback;
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}

