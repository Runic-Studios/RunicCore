package com.runicrealms.plugin.spellapi.effect.mage;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class IncendiaryEffect implements SpellEffect {
    private final Player caster;
    private final double duration;
    private long startTime;

    /**
     * @param caster   player who is incendiary
     * @param duration (in seconds) before the effect expires
     */
    public IncendiaryEffect(Player caster, double duration) {
        this.caster = caster;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        executeSpellEffect();
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
            return;
        }
//        if (globalCounter % 20 == 0) { // Show particle once per second
//            executeSpellEffect();
//        }
    }

    @Override
    public void executeSpellEffect() {
//        new HelixParticleFrame(1.0F, 30, 20.0F).playParticle(caster, Particle.FLAME, caster.getLocation());
        // TODO: only once
        spawn(caster.getLocation());
    }

    public ModeledEntity spawn(Location location) {

        // Center gravestone location in the block
//        gravestoneLocation = gravestoneLocation.getBlock().getLocation().add(0.5f, 0, 0.5f);

        // Spawn a base entity
        Dummy<?> dummy = new Dummy<>();
        dummy.setLocation(location);

        ActiveModel activeModel = ModelEngineAPI.createActiveModel("meteor_storm_magic_circle");
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);

        if (activeModel != null) {
            activeModel.setHitboxVisible(true);
            activeModel.setHitboxScale(4.0);
            modeledEntity.addModel(activeModel, true);
        }

        return modeledEntity;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

}
