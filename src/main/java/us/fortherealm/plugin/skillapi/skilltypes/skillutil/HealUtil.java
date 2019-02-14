package us.fortherealm.plugin.skillapi.skilltypes.skillutil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import us.fortherealm.plugin.item.GearScanner;

public class HealUtil  {

    @SuppressWarnings("deprecation")
    public void healPlayer(double healAmt, Player recipient, Player caster, String sourceStr) {

        healAmt = healAmt + GearScanner.getHealingBoost(caster);
        double newHP = recipient.getHealth() + healAmt;
        double difference = recipient.getMaxHealth() - recipient.getHealth();

        // if they are missing less than healAmt
        if (newHP > recipient.getMaxHealth()) {
            recipient.setHealth(recipient.getMaxHealth());
            if (difference != (int) difference) {
                recipient.sendMessage(ChatColor.GREEN + "+" + ((int) difference + 1) + " ❤" + sourceStr);
            }
            if (difference == (int) difference) {
                recipient.sendMessage(ChatColor.GREEN + "+" + ((int) difference) + " ❤" + sourceStr);
            }
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        } else {
            recipient.setHealth(newHP);
            recipient.sendMessage(ChatColor.GREEN + "+" + (int) healAmt + " ❤" + sourceStr);
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        }

        // call a new health regen event to communicate with all the other events that depend on this.
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, healAmt, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }
}

