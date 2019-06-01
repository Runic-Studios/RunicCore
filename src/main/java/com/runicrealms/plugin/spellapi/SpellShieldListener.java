package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

/**
 * Manages the spell 'shield' effect, which is like a heal, but can overlay health and is treated separately.
 */
public class SpellShieldListener {

    /**
     * Listen for damage and block w/ shield
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if (!(e.getEntity() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        UUID id = victim.getUniqueId();

        if (!HealUtil.getShieldedPlayers().containsKey(id)) return;

        int shieldAmt = HealUtil.getShieldedPlayers().get(id);
        int difference = shieldAmt - (int) e.getDamage();

        if (difference >= 0) {

            HealUtil.getShieldedPlayers().put(id, difference);
            HologramUtil.createShieldDamageHologram(victim, victim.getLocation().add(0, 1.5, 0), e.getDamage());

        } else {

            e.setDamage(difference);
            HealUtil.getShieldedPlayers().remove(id);
        }
    }
}
