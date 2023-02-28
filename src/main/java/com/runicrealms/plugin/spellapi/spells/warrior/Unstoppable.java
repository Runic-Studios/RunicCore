package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.api.event.StatusEffectEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class Unstoppable extends Spell {

    public Unstoppable() {
        super("Unstoppable",
                "While you are affected by any speed boost effects, you become immune to stuns, " +
                        "slows and roots!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    /**
     * Prevent warriors with unstoppable passive from being CC'ed while affected by speed
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrainingHit(StatusEffectEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getLivingEntity().getUniqueId(), this.getName())) return;
        // todo: slows?
        if (!(event.getRunicStatusEffect() == RunicStatusEffect.STUN || event.getRunicStatusEffect() == RunicStatusEffect.ROOT))
            return;
        if (!(hasStatusEffect(event.getLivingEntity().getUniqueId(), RunicStatusEffect.SPEED_I)
                || hasStatusEffect(event.getLivingEntity().getUniqueId(), RunicStatusEffect.SPEED_II)
                || hasStatusEffect(event.getLivingEntity().getUniqueId(),
                RunicStatusEffect.SPEED_III))) {
            return;
        }
        event.setCancelled(true);
    }
}

