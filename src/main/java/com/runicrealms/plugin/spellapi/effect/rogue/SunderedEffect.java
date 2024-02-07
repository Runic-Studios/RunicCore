package com.runicrealms.plugin.spellapi.effect.rogue;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SunderedEffect implements SpellEffect {
    private final Player caster;
    private final LivingEntity recipient;
    private final double duration;
    private long startTime;

    /**
     * @param caster    player who caused the effect
     * @param recipient entity who is affected
     * @param duration  before the effect expires
     */
    public SunderedEffect(Player caster, LivingEntity recipient, double duration) {
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
        return SpellEffectType.SUNDERED;
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
        caster.playSound(recipient.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.2f, 2.0f);
        caster.spawnParticle(
                Particle.BLOCK_CRACK,
                recipient.getEyeLocation(),
                15,
                Math.random() * 1.5,
                Math.random() / 2,
                Math.random() * 1.5,
                Material.PACKED_ICE.createBlockData()
        );
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
