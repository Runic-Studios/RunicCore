package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RunicDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.cleric.AriaOfArmorEffect;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * the first passive for bard
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Accelerando extends Spell implements DurationSpell, RadiusSpell, AttributeSpell, Tempo.Influenced {
    private static final Stat STAT = Stat.INTELLIGENCE;
    private double base;
    private double duration;
    private double multiplier;
    private double radius;

    public Accelerando() {
        super("Accelerando", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Whenever you cast a &6Bard&7 spell, " +
                "you and all allies within " + this.radius + " blocks gain " +
                "Speed II and &faria of armor &7for " + this.duration + "s!" +
                "\n\n&2&lEFFECT &fAria of Armor" +
                "\n&7Allies affected by &faria of armor &7gain (" + this.base + " + &f" + this.multiplier + "x&e " +
                STAT.getPrefix() + "&7)% damage reduction!");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onSpellCast(SpellCastEvent event) {
        if (!this.hasPassive(event.getCaster().getUniqueId(), this.getName())) {
            return;
        }

        if (!(event.getSpell() instanceof Battlecry
                || event.getSpell() instanceof Powerslide
                || event.getSpell() instanceof GrandSymphony)) {
            return;
        }

        Player player = event.getCaster();
        for (Entity entity : event.getCaster().getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (!(entity instanceof Player ally) || !this.isValidAlly(event.getCaster(), ally)) {
                continue;
            }
            this.removeExtraDuration(ally);
            this.applySpeed(player, ally);
            applyAriaOfArmor(player, ally);
        }

        this.removeExtraDuration(event.getCaster());
        this.applySpeed(player, player);
        applyAriaOfArmor(player, player); // Apply song of war to caster, since we're not using .getWorld() for entity check
    }

    private void applyAriaOfArmor(Player player, Player recipient) {
        player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, recipient.getEyeLocation(), 8, Math.random() * 2, Math.random(), Math.random() * 2);
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), recipient.getUniqueId(), SpellEffectType.ARIA_OF_ARMOR);
        if (spellEffectOpt.isPresent()) {
            AriaOfArmorEffect ariaOfArmorEffect = (AriaOfArmorEffect) spellEffectOpt.get();
            ariaOfArmorEffect.refresh();
        } else {
            AriaOfArmorEffect ariaOfArmorEffect = new AriaOfArmorEffect(recipient, this.duration);
            ariaOfArmorEffect.initialize();
        }
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
    
    private void onMobDamage(MobDamageEvent event) {

    }

    /**
     * A method used to reduce the damage of a given damage event
     *
     * @param event the event
     */
    private void reduceDamage(@NotNull RunicDamageEvent event) {
//        if (!(event.getVictim() instanceof Player player)) {
//            return;
//        }
//
//        Pair<Integer, Long> data = this.damageReduction.get(event.getVictim().getUniqueId());
//
//        if (data == null || System.currentTimeMillis() > data.second + (this.getDuration(player) * 1000)) {
//            this.removeExtraDuration(player);
//            return; //if not in map or they are in map but the duration is already over
//        }
//
//        double percent = (this.base + (data.first * this.multiplier)) / 100;
//        Bukkit.broadcastMessage("percent is " + percent);
//        int amount = (int) (event.getAmount() * percent);
//        event.setAmount(event.getAmount() - amount);
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

//    @Override
//    public void increaseExtraDuration(@NotNull Player player, double seconds) {
//        Tempo.Influenced.super.increaseExtraDuration(player, seconds);
//
//        double duration = RunicCore.getStatusEffectAPI().getStatusEffectDuration(player.getUniqueId(), RunicStatusEffect.SPEED_II);
//
//        if (duration > 0) {
//            this.addStatusEffect(player, RunicStatusEffect.SPEED_II, duration + seconds, false);
//        }
//    }

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
