package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StaticEffect implements SpellEffect {
    private final Player caster;
    private final LivingEntity recipient;
    private final double duration;
    private long startTime;

    /**
     * @param caster    player who caused the bleed
     * @param recipient entity who is bleeding
     * @param duration  before the effect expires
     */
    public StaticEffect(Player caster, LivingEntity recipient, double duration) {
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
        return SpellEffectType.STATIC;
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
        if (globalCounter % 10 == 0) { // Show particle effect twice per second
            executeSpellEffect();
        }
    }

    @Override
    public void executeSpellEffect() {
        caster.playSound(recipient.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.25f, 1.0f);
        caster.spawnParticle(Particle.BLOCK_CRACK, recipient.getEyeLocation(), 10, Math.random() * 1.5, Math.random() / 2, Math.random() * 1.5, Material.LAPIS_BLOCK.createBlockData());
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
