package com.runicrealms.plugin.spellapi.effect.mage;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class IgniteEffect implements SpellEffect {
    private final Player caster;
    private final LivingEntity recipient;
    private final double duration;
    private long startTime;

    public IgniteEffect(Player caster, LivingEntity recipient, double duration) {
        this.caster = caster;
        this.recipient = recipient;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.IGNITED;
    }

    @Override
    public boolean isBuff() {
        return false;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public double getDuration() {
        return duration;
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
        caster.playSound(recipient.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.25f, 1.0f);
        Cone.coneEffect(recipient, Particle.FLAME, 1, 0, 20, Color.RED);
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }
}
