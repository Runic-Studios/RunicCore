package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.rogue.BetrayedEffect;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

public class Backstab extends Spell implements AttributeSpell, DurationSpell {
    private static final double BEHIND_THRESHOLD = 0.75;
    private double baseValue;
    private double duration;
    private double multiplier;
    private String statName;

    public Backstab() {
        super("Backstab", CharacterClass.ROGUE);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("Basic attacking an enemy from behind causes " +
                "&cbetrayed &7for " + duration + "s! Subsequent attacks refresh " +
                "the duration." +
                "\n\n&2&lEFFECT &cBetrayed" +
                "\n&cBetrayed &7enemies suffer (" + baseValue + " + " + multiplier + "x " + prefix + ")% " +
                "extra physical⚔ damage from your basic attacks!");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) // runs last
    public void onBackstab(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        UUID uuid = event.getPlayer().getUniqueId();
        UUID victimId = event.getVictim().getUniqueId();
        // Logic for damage ramp
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, victimId, SpellEffectType.BETRAYED);
        if (spellEffectOpt.isPresent()) {
            double bonusDamage = event.getAmount() * this.percentAttribute(event.getPlayer());
            event.setAmount((int) (event.getAmount() + bonusDamage));
            event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 0.25F);
            event.getVictim().getWorld().spawnParticle
                    (Particle.VILLAGER_ANGRY, event.getVictim().getEyeLocation(), 5, 1.0F, 0, 0, 0); // 0.3F
        }
        // Logic for applying betrayed
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!isBehind(event.getPlayer(), event.getVictim())) return;
        event.getPlayer().getWorld().playSound(event.getVictim().getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 0.5f, 2.0f);
        if (spellEffectOpt.isPresent()) {
            BetrayedEffect betrayedEffect = (BetrayedEffect) spellEffectOpt.get();
            betrayedEffect.refresh();
        } else {
            BetrayedEffect betrayedEffect = new BetrayedEffect(event.getPlayer(), event.getVictim(), this.duration);
            betrayedEffect.initialize();
        }
    }

    /**
     * @return true if the attacker is behind the victim
     */
    private boolean isBehind(LivingEntity attacker, LivingEntity victim) {
        // Get the location vectors of both entities
        Location attackerLocation = attacker.getLocation();
        Location victimLocation = victim.getLocation();

        // Calculate the direction vector of the victim
        Vector toAttacker = attackerLocation.toVector().subtract(victimLocation.toVector()).normalize();

        // Get the direction vector the victim is facing
        Vector victimDirection = victimLocation.getDirection().normalize();

        // Calculate the dot product between the direction vector and the vector from victim to attacker
        double dot = victimDirection.dot(toAttacker);

        // If the dot product is negative, the attacker is behind the victim
        return dot < 0;
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
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}

