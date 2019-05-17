package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.item.GearScanner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import com.runicrealms.plugin.RunicCore;

public class HealUtil  {

    @SuppressWarnings("deprecation")
    public static void healPlayer(double healAmt, Player recipient, Player caster) {

        String storedName = RunicCore.getInstance().getConfig().get(caster.getUniqueId() + ".info.name").toString();
        String sourceStr = " from " + ChatColor.WHITE + storedName;
        if (recipient == caster) sourceStr = "";

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
                if (difference <= 0) {
                    return;
                }
                recipient.sendMessage(ChatColor.GREEN + "+" + ((int) difference) + " ❤" + sourceStr);
            }
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
            recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);
        } else {
            recipient.setHealth(newHP);
            recipient.sendMessage(ChatColor.GREEN + "+" + (int) healAmt + " ❤" + sourceStr);
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
            recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);
        }

        // call a new health regen event to communicate with all the other events that depend on this.
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, healAmt, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }
}


