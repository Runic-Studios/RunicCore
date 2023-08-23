package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.statuseffects.EntityBleedEvent;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * the first passive for bard
 *
 * @author BoBoBalloon
 */
public class Accelerando extends Spell implements DurationSpell, RadiusSpell, AttributeSpell, Tempo.Influenced {
    private static final Stat STAT = Stat.INTELLIGENCE;
    private final Map<UUID, Pair<Integer, Long>> damageReduction;
    private double duration;
    private double radius;
    private double base;
    private double multiplier;

    public Accelerando() {
        super("Accelerando", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Whenever you cast a &6Bard&7 spell, you and all allies within " + this.radius + " block radius gain\n" +
                "Speed II and (" + this.base + " + &f" + this.multiplier + "x&e " + STAT.getPrefix() + "&7)% damage reduction for " + this.duration + "s.");
        this.damageReduction = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        if (!(event.getSpell() instanceof Battlecry || event.getSpell() instanceof Powerslide || event.getSpell() instanceof GrandSymphony)) {
            return;
        }

        int stat = RunicCore.getStatAPI().getStat(event.getCaster().getUniqueId(), STAT.getIdentifier());
        long now = System.currentTimeMillis();

        for (Entity entity : event.getCaster().getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (!(entity instanceof Player ally) || !this.isValidAlly(event.getCaster(), ally)) {
                continue;
            }

            this.removeExtraDuration(ally);
            this.applySpeed(event.getCaster(), ally);
            this.damageReduction.put(ally.getUniqueId(), Pair.pair(stat, now));
        }

        this.removeExtraDuration(event.getCaster());
        this.applySpeed(event.getCaster(), event.getCaster());
        this.damageReduction.put(event.getCaster().getUniqueId(), Pair.pair(stat, now));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onPhysicalDamage(PhysicalDamageEvent event) {
        this.reduceDamage(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        this.reduceDamage(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onEnvironmentDamage(EnvironmentDamageEvent event) {
        this.reduceDamage(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onBleed(EntityBleedEvent event) {
        this.reduceDamage(event);
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.damageReduction.remove(event.getPlayer().getUniqueId());
    }

    /**
     * A method used to reduce the damage of a given damage event
     *
     * @param event the event
     */
    private void reduceDamage(@NotNull RunicDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) {
            return;
        }

        Pair<Integer, Long> data = this.damageReduction.get(event.getVictim().getUniqueId());

        if (data == null || System.currentTimeMillis() > data.second + (this.getDuration(player) * 1000)) {
            this.removeExtraDuration(player);
            return; //if not in map or they are in map but the duration is already over
        }

        double percent = (this.base + (data.first * this.multiplier)) / 100;
        int amount = (int) (event.getAmount() * percent);
        event.setAmount(event.getAmount() - amount);
    }

    /**
     * @param target to receive speed
     * @author Skyfallin
     */
    private void applySpeed(@NotNull Player caster, @NotNull LivingEntity target) {
        // Begin sound effects
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);
        // Add player effects
        addStatusEffect(target, RunicStatusEffect.SPEED_II, this.getDuration(caster), false);
        target.getWorld().spawnParticle(Particle.REDSTONE, target.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
    }

    @Override
    public void increaseExtraDuration(@NotNull Player player, double seconds) {
        Tempo.Influenced.super.increaseExtraDuration(player, seconds);

        double duration = RunicCore.getStatusEffectAPI().getStatusEffectDuration(player.getUniqueId(), RunicStatusEffect.SPEED_II);

        if (duration > 0) {
            this.addStatusEffect(player, RunicStatusEffect.SPEED_II, duration + seconds, false);
        }
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
        return this.base;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.base = baseValue;
    }

    @Override
    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return STAT.getIdentifier();
    }

    @Override
    @Deprecated
    public void setStatName(String statName) {

    }
}
