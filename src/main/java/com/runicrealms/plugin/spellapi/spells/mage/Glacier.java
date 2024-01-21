package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.spellapi.effect.IceBarrierEffect;
import com.runicrealms.plugin.spellapi.effect.event.SpellEffectEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;
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
    public void onBasicAttack(BasicAttackEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!hasPassive(uuid, this.getName())) return;
        // todo: if doesnt have effect return
        // todo: if doesnt have max stacks return
        // todo: if max stacks, slow
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        UUID uuid = event.getVictim().getUniqueId();
        if (!hasPassive(uuid, this.getName())) return;
        // todo: if doesnt have effect return
        // todo: if doesnt have max stacks return
        // todo: if max stacks, slow
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }
}

