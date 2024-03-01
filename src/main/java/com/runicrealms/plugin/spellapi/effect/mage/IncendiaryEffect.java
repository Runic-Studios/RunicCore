package com.runicrealms.plugin.spellapi.effect.mage;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellAttached;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class IncendiaryEffect implements SpellEffect {
    private static final String MODEL_ID = "incendiary_indicator";
    private final Player caster;
    private final double duration;
    private final ModeledSpellAttached modeledSpellAttached;
    private long startTime;

    /**
     * @param caster   player who is incendiary
     * @param duration (in seconds) before the effect expires
     */
    public IncendiaryEffect(Player caster, double duration) {
        this.caster = caster;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.modeledSpellAttached = spawnModel();
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
        modeledSpellAttached.cancel();
    }

    public void refresh() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.INCENDIARY;
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
        }
    }

    @Override
    public void executeSpellEffect() {

    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    private ModeledSpellAttached spawnModel() {
        ModeledSpellAttached modeledSpellAttached = new ModeledSpellAttached(
                caster,
                MODEL_ID,
                this.caster.getLocation(),
                1.0,
                this.duration,
                target -> false
        );
        modeledSpellAttached.initialize();
        modeledSpellAttached.getModeledEntity().getModels().forEach((s, activeModel) -> activeModel.getAnimationHandler().playAnimation(
                "idle",
                0.5,
                0.5,
                1.0,
                false
        ));
        return modeledSpellAttached;
    }

}
