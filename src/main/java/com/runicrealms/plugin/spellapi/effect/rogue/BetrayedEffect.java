package com.runicrealms.plugin.spellapi.effect.rogue;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BetrayedEffect implements SpellEffect {
    private final Player caster;
    private final LivingEntity recipient;
    private final double duration;
    private long startTime;

    /**
     * @param caster    player who caused the bleed
     * @param recipient entity who is bleeding
     * @param duration  before the effect expires
     */
    public BetrayedEffect(Player caster, LivingEntity recipient, double duration) {
        this.caster = caster;
        this.recipient = recipient;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    @Override
    public double getDuration() {
        return duration;
    }

    public void refresh() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.BETRAYED;
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
        if (globalCounter % 20 == 0) { // Show particle effect once per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        caster.playSound(recipient.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.2f, 0.2f);
        caster.spawnParticle(
                Particle.VILLAGER_ANGRY,
                recipient.getEyeLocation(),
                5,
                0.35f,
                0.35f,
                0.35f,
                0
        );
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
