package com.runicrealms.plugin.spellapi.effect.cleric;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BalladOfBindingEffect implements SpellEffect {
    private final Player caster;
    private final LivingEntity recipient;
    private final double duration;
    private long startTime;

    /**
     * @param caster    player who caused the effect
     * @param recipient living entity who is receiving the effect
     * @param duration  (in seconds) before the effect expires
     */
    public BalladOfBindingEffect(Player caster, LivingEntity recipient, double duration) {
        this.caster = caster;
        this.recipient = recipient;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    public void refresh() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.BALLAD_OF_BINDING;
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
        if (recipient.isDead()) {
            this.cancel();
            return;
        }
        if (globalCounter % 20 == 0) { // Show particle once per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        recipient.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, recipient.getEyeLocation(), 8, Math.random() * 2, Math.random(), Math.random() * 2);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
