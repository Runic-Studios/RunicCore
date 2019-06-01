package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Manages the spell 'shield' effect, which is like a heal, but can overlay health and is treated separately.
 */
public class SpellShieldListener implements Listener {

    /**
     * Listen for damage and block w/ shield
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {

        if (!(e.getEntity() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        UUID id = victim.getUniqueId();
        int difference = difference(victim, e.getAmount());

        if (difference == -1) {
            return;
        }

        if (difference >= 0) {

            HealUtil.getShieldedPlayers().put(id, difference);
            HologramUtil.createShieldDamageHologram(victim, victim.getLocation().add(0, 1.5, 0), e.getAmount());
            e.setAmount(0);

        } else {

            e.setAmount(difference);
            HealUtil.getShieldedPlayers().remove(id);
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {

        if (!(e.getEntity() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        UUID id = victim.getUniqueId();
        int difference = difference(victim, e.getAmount());

        if (difference == -1) {
            return;
        }

        if (difference >= 0) {

            HealUtil.getShieldedPlayers().put(id, difference);
            HologramUtil.createShieldDamageHologram(victim, victim.getLocation().add(0, 1.5, 0), e.getAmount());
            e.setAmount(0);

        } else {

            e.setAmount(difference);
            HealUtil.getShieldedPlayers().remove(id);
        }
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {

        if (!(e.getVictim() instanceof Player)) return;

        Player victim = (Player) e.getVictim();
        UUID id = victim.getUniqueId();
        int difference = difference(victim, e.getAmount());

        if (difference == -1) {
            return;
        }

        if (difference >= 0) {

            HealUtil.getShieldedPlayers().put(id, difference);
            HologramUtil.createShieldDamageHologram(victim, victim.getLocation().add(0, 1.5, 0), e.getAmount());
            e.setAmount(0);

        } else {

            e.setAmount(difference);
            HealUtil.getShieldedPlayers().remove(id);
        }
    }

    private int difference(Player victim, int amount) {

        UUID id = victim.getUniqueId();

        if (!HealUtil.getShieldedPlayers().containsKey(id)) {
            return -1;
        }

        int shieldAmt = HealUtil.getShieldedPlayers().get(id);
        return shieldAmt - amount;
    }
}
