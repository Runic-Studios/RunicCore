package com.runicrealms.plugin.spellapi.effect.cleric;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellAttached;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * The type Allegro effect.
 */
public class AllegroEffect implements SpellEffect {
    private static final String MODEL_ID = "blue_bird";
    private final Player recipient;
    private final double duration;
    private final ModeledSpellAttached modeledSpellAttached;
    private long startTime;

    /**
     * Instantiates a new Allegro effect.
     *
     * @param recipient player who is receiving the effect
     * @param duration  (in seconds) before the effect expires
     */
    public AllegroEffect(Player recipient, double duration) {
        this.recipient = recipient;
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
        this.modeledSpellAttached.cancel();
    }

    /**
     * Refresh.
     */
    public void refresh() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.ARIA_OF_ARMOR;
    }

    @Override
    public boolean isBuff() {
        return true;
    }

    @Override
    public Player getCaster() {
        return recipient;
    }

    @Override
    public LivingEntity getRecipient() {
        return recipient;
    }

    @Override
    public void tick(int globalCounter) {
        if (recipient.isDead()) {
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
                recipient,
                MODEL_ID,
                this.recipient.getLocation(),
                0,
                this.duration,
                null
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
