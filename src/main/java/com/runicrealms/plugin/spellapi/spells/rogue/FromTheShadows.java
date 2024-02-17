package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spells.Combat;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FromTheShadows extends Spell implements DurationSpell {
    private final Map<UUID, Integer> buffedPlayersMap = new HashMap<>(); // used so both Twin Fangs get buff
    private double duration;

    public FromTheShadows() {
        super("From The Shadows", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("While you are &8shrouded&7, your first " +
                "spell cast is empowered! " +
                "\n\n&aDash &7- You cleanse all slows and gain Speed III for " + this.duration + "s!" +
                "\n\n&aTwin Fangs &7- This spell will critically strike!" +
                "\n\n&aCocoon &7- Successfully landing this spell will teleport you behind your opponent!"
        );
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEmpoweredSpell(PhysicalDamageEvent event) {
        // Check if the player has the relevant passive ability
        UUID casterId = event.getPlayer().getUniqueId();
        if (!hasPassive(casterId, this.getName())) return;

        // If the spell is Twin Fangs, mark the event as critical and only reduce 1 stack (so that both fangs get buff)
        if (event.getSpell() instanceof TwinFangs) {
            buffedPlayersMap.put(casterId, buffedPlayersMap.get(casterId) - 1);
            event.setCritical(true);
        }
        // Reduce a buff stack (or remove buff) for the player.
        if (buffedPlayersMap.get(casterId) == 0) {
            buffedPlayersMap.remove(casterId);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        Cocoon cocoon = (Cocoon) RunicCore.getSpellAPI().getSpell("Cocoon");
        if (cocoon == null) {
            throw new IllegalStateException("Expected spell was not registered in the SpellManager");
        }

        if (this.hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.SHROUDED)) {
            // TODO: fix this
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!hasSpellEffect(event.getCaster().getUniqueId(), SpellEffectType.SHROUDED)) return;
        if (event.getSpell() instanceof Combat || event.getSpell() instanceof Potion) {
            return;
        }

        if (event.getSpell() instanceof Dash) {
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_I);
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_II);
            this.removeStatusEffect(event.getCaster(), RunicStatusEffect.SLOW_III);
            this.addStatusEffect(event.getCaster(), RunicStatusEffect.SPEED_III, 4, true);
        } else if (event.getSpell() instanceof TwinFangs) {
            // TODO: fix
            buffedPlayersMap.put(event.getCaster().getUniqueId(), 2);
        }
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}

