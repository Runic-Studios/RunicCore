package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Map;

public class GiftsOfTheGrove extends Spell implements AttributeSpell, DurationSpell {
    private double baseValue;
    private double multiplier;
    private double percent;
    private String statName;
    private double duration;

    public GiftsOfTheGrove() {
        super("Gifts Of The Grove", CharacterClass.ARCHER);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("While inside your &aSacred Grove&7, " +
                "your healing✦ is increased by (" + baseValue + " + &f" + multiplier + "x &e" + prefix + "&7)% " +
                "and when you hit an enemy the cooldown for &aRemedy&7 is reduced by " + this.duration + "s " +
                "If you are inside the grove when it expires, " +
                "it releases one more pulse, " +
                "healing allies for " + (percent * 100) + "% of its base value!");
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
        setPercent(percent.doubleValue());
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @EventHandler
    public void onGroveExpiry(SacredGrove.GroveExpiryEvent event) {
        if (event.isCancelled()) return;
        Location groveLocation = SacredGrove.getGroveLocationMap().get(event.getCaster().getUniqueId());
        if (groveLocation == null) return;
        Spell spell = RunicCore.getSpellAPI().getSpell("Sacred Grove");
        double radius = ((RadiusSpell) spell).getRadius();
        // Ensure player is within grove radius
        if (event.getCaster().getLocation().distanceSquared(groveLocation) > radius * radius)
            return;
        double heal = ((HealingSpell) spell).getHeal();
        double healPerLevel = ((HealingSpell) spell).getHealingPerLevel();
        double total = heal + (healPerLevel * event.getCaster().getLevel());
        for (Entity entity : groveLocation.getWorld().getNearbyEntities(groveLocation, radius, radius, radius, target -> isValidAlly(event.getCaster(), target))) {
            Player playerEntity = (Player) entity;
            healPlayer(event.getCaster(), playerEntity, percent * total, this);
        }
        groveLocation.getWorld().playSound(groveLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.25f);
        groveLocation.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, groveLocation, 15, radius, 3, radius, 0);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof SacredGrove)) return;
        Location groveLocation = SacredGrove.getGroveLocationMap().get(event.getPlayer().getUniqueId());
        if (groveLocation == null) return;
        double radius = ((RadiusSpell) RunicCore.getSpellAPI().getSpell("Sacred Grove")).getRadius();
        // Ensure player is within grove radius
        if (event.getPlayer().getLocation().distanceSquared(groveLocation) > radius * radius)
            return;
        int stat = RunicCore.getStatAPI().getStat(event.getPlayer().getUniqueId(), this.statName);
        double bonus = (multiplier * stat) / 100;
        event.setAmount((int) (event.getAmount() + (event.getAmount() * bonus)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onRangedPhysicalDamage(PhysicalDamageEvent event) {
        if (!event.isRanged() || !this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !RunicCore.getSpellAPI().isOnCooldown(event.getPlayer(), "Remedy")) {
            return;
        }

        Location groveLocation = SacredGrove.getGroveLocationMap().get(event.getPlayer().getUniqueId());

        if (groveLocation == null) {
            return;
        }

        double radius = ((RadiusSpell) RunicCore.getSpellAPI().getSpell("Sacred Grove")).getRadius();
        if (event.getPlayer().getLocation().distanceSquared(groveLocation) > radius * radius) {
            return; //outside of radius
        }

        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Remedy", this.duration);
    }
}
