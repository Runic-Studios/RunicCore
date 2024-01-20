package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.IceBarrierEffect;
import com.runicrealms.plugin.spellapi.effect.event.SpellEffectEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
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
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 5);
        setMaxStacks(maxStacks.doubleValue());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpellCast(SpellEffectEvent event) {
        UUID uuid = event.getSpellEffect().getCaster().getUniqueId();
        if (!hasPassive(uuid, this.getName())) return;
        if (!(event.getSpellEffect() instanceof IceBarrierEffect iceBarrierEffect)) return;
        Bukkit.broadcastMessage("sanity ice barrier found");
        iceBarrierEffect.setMaxStacks((int) this.maxStacks);
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }
}

