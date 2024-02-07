package com.runicrealms.plugin.spellapi.effect.warrior;

import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.StackEffect;
import com.runicrealms.plugin.spellapi.effect.StackHologram;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BleedEffect implements StackEffect {
    public static final int DAMAGE_CAP = 100;
    public static final double HEALING_REDUCTION = .25;
    private static final int DEFAULT_STACKS = 3;
    private static final int PERIOD = 40;
    private static final double MAX_HEALTH_PERCENT = .03;
    private final Player caster;
    private final LivingEntity recipient;
    private final StackHologram stackHologram;
    private int nextTickCounter;
    private AtomicInteger stacks;

    /**
     * @param caster    player who caused the bleed
     * @param recipient entity who is bleeding
     */
    public BleedEffect(Player caster, LivingEntity recipient) {
        this.caster = caster;
        this.recipient = recipient;
        this.stacks = new AtomicInteger(DEFAULT_STACKS);
        this.stackHologram = new StackHologram(
                SpellEffectType.BLEED,
                caster.getLocation(),
                Set.of(caster)
        );
    }

    public void refreshStacks() {
        this.stacks = new AtomicInteger(DEFAULT_STACKS);
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.BLEED;
    }

    @Override
    public boolean isActive() {
        return stacks.get() > 0;
    }

    @Override
    public boolean isBuff() {
        return false;
    }

    @Override
    public Player getCaster() {
        return caster;
    }

    @Override
    public LivingEntity getRecipient() {
        return recipient;
    }

    @Override
    public void tick(int globalCounter) {
        if (globalCounter < nextTickCounter) {
            return;
        }
        if (recipient.isDead()) {
            cancel();
            return;
        }
        if (stacks.get() > 0) {
            executeSpellEffect();
            stacks.getAndDecrement();
        }
        // Set the next tick
        nextTickCounter += getTickInterval();
    }

    @Override
    public void executeSpellEffect() {
        recipient.getWorld().playSound(recipient.getLocation(), Sound.ENTITY_COD_HURT, 0.5f, 1.0f);
        recipient.getWorld().spawnParticle(Particle.BLOCK_CRACK, recipient.getEyeLocation(), 10, Math.random() * 1.5, Math.random() / 2, Math.random() * 1.5, Material.REDSTONE_BLOCK.createBlockData());
        double percentMaxHealthAmount = recipient.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * MAX_HEALTH_PERCENT;
        DamageUtil.damageEntityPhysical(Math.min(percentMaxHealthAmount, 100), recipient, caster, false, false, false);
        this.stackHologram.showHologram(this.recipient.getEyeLocation().add(0, 1.5f, 0), this.stacks.get());
    }

    @Override
    public void cancel() {
        stacks.getAndSet(0);
    }

    @Override
    public void setNextTickCounter(int nextTickCounter) {
        this.nextTickCounter = nextTickCounter;
    }

    @Override
    public AtomicInteger getStacks() {
        return stacks;
    }

    @Override
    public int getTickInterval() {
        return PERIOD;
    }

    @Override
    public StackHologram getStackHologram() {
        return stackHologram;
    }
}
