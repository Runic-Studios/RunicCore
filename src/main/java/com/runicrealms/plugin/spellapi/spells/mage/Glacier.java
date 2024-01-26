package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.IceBarrierEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class Glacier extends Spell implements DurationSpell {
    private double duration;
    private double maxStacks;

    public Glacier() {
        super("Glacier", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("You can now reach " + maxStacks + " ice barrier stacks! " +
                "While at max stacks, mobs and players who hit you with " +
                "basic attacks are slowed for " + duration + "s!");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 5);
        setMaxStacks(maxStacks.doubleValue());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpellCast(SpellEffectEvent event) {
        UUID uuid = event.getSpellEffect().getCaster().getUniqueId();
        if (!hasPassive(uuid, this.getName())) return;
        if (!(event.getSpellEffect() instanceof IceBarrierEffect iceBarrierEffect)) return;
        iceBarrierEffect.setMaxStacks((int) this.maxStacks);
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        UUID victimId = event.getVictim().getUniqueId();
        if (!hasPassive(victimId, this.getName())) return;
        if (!hasMaxIceBarrierStacks(victimId)) return;
        this.addStatusEffect(event.getPlayer(), RunicStatusEffect.SLOW_II, this.duration, false, event.getPlayer());
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        UUID uuid = event.getVictim().getUniqueId();
        if (!hasPassive(uuid, this.getName())) return;
        if (!hasMaxIceBarrierStacks(uuid)) return;
        this.addStatusEffect(event.getVictim(), RunicStatusEffect.SLOW_II, this.duration, false);
    }

    private boolean hasMaxIceBarrierStacks(UUID uuid) {
        Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(uuid, uuid, SpellEffectType.ICE_BARRIER);
        if (spellEffectOpt.isEmpty()) {
            return false;
        } else {
            IceBarrierEffect iceBarrierEffect = (IceBarrierEffect) spellEffectOpt.get();
            return iceBarrierEffect.getStacks().get() == this.maxStacks;
        }
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }
}

