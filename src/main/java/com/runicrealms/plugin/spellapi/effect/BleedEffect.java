package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BleedEffect implements SpellEffect {
    public static final int DAMAGE_CAP = 100;
    public static final double HEALING_REDUCTION = .25;
    private static final int DEFAULT_STACKS = 3;
    private static final int PERIOD = 40;
    private static final double MAX_HEALTH_PERCENT = .03;
    private final Player caster;
    private final LivingEntity recipient;
    private final Spell spellSource;
    private int stacksRemaining;

    /**
     * @param caster      uuid of the caster
     * @param recipient   uuid of the victim
     * @param spellSource the spell which caused the bleed (for scaling)
     */
    public BleedEffect(Player caster, LivingEntity recipient, Spell spellSource) {
        this.caster = caster;
        this.recipient = recipient;
        this.spellSource = spellSource;
        this.stacksRemaining = DEFAULT_STACKS;
    }

    public Spell getSpellSource() {
        return spellSource;
    }

    public void refreshStacks() {
        this.stacksRemaining = DEFAULT_STACKS;
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.BLEED;
    }

    @Override
    public boolean isActive() {
        return stacksRemaining > 0;
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
    public void tick(int counter) {
        if (recipient.isDead()) {
            stacksRemaining = 0;
            return;
        }
        if (counter % PERIOD == 0) {
            if (stacksRemaining > 0) {
                bleedEffect();
                stacksRemaining--;
            }
        }
    }

    private void bleedEffect() {
        recipient.getWorld().playSound(recipient.getLocation(), Sound.ENTITY_COD_HURT, 0.5f, 1.0f);
        recipient.getWorld().spawnParticle(Particle.BLOCK_CRACK, recipient.getEyeLocation(), 10, Math.random() * 1.5, Math.random() / 2, Math.random() * 1.5, Material.REDSTONE_BLOCK.createBlockData());
        double percentMaxHealthAmount = recipient.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * MAX_HEALTH_PERCENT;
        DamageUtil.damageEntityPhysical(Math.min(percentMaxHealthAmount, 100), recipient, caster, false, false, false);
    }

    @Override
    public int getTickInterval() {
        return PERIOD;
    }
}
