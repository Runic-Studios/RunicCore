package com.runicrealms.plugin.spellapi.effect.mage;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ArcanumEffect implements SpellEffect {
    private final Player caster;
    private final double duration;
    private long startTime;

    /**
     * @param caster   player who has effect
     * @param duration (in seconds) before the effect expires
     */
    public ArcanumEffect(Player caster, double duration) {
        this.caster = caster;
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
        return SpellEffectType.ARCANUM;
    }

    @Override
    public boolean isBuff() {
        return true;
    }

    @Override
    public Player getCaster() {
        return caster;
    }

    @Override
    public LivingEntity getRecipient() {
        return caster;
    }

    @Override
    public void tick(int globalCounter) {
        if (caster.isDead()) {
            this.cancel();
            return;
        }
        if (globalCounter % 20 == 0) { // Show particle once per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        new HelixParticleFrame(1.0F, 30, 20.0F).playParticle(caster, Particle.SPELL_WITCH, caster.getLocation());
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
