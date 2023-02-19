package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.SpellShieldEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class ShieldListener implements Listener {
    private static final int HALF_HEART_AMOUNT = 50; // how much health is 1/2 heart?

    // todo: task to remove shield after 5s

    /**
     * Damages a player's shield. If there's still shield left, updates the displayed yellow hearts.
     * Otherwise, returns the damage the player should take
     *
     * @param player who was damaged
     * @param damage the player received
     * @return the damage left over after the shield
     */
    private double damageShield(Player player, double damage) {
        double shield = RunicCore.getSpellAPI().getShieldedPlayers().get(player.getUniqueId());
        double shieldLeftOver = shield - damage;
        Map<UUID, Double> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        if (shieldLeftOver > 0) {
            player.setAbsorptionAmount(shieldLeftOver / HALF_HEART_AMOUNT);
            shieldedPlayers.put(player.getUniqueId(), shieldLeftOver);
        } else if (shieldLeftOver <= 0) {
            // Shield was broken and there's leftover damage
            player.setAbsorptionAmount(0);
            shieldedPlayers.remove(player.getUniqueId());
        }
        return shieldLeftOver;
    }

    // todo: add wisdom to shield in stat listener, check the priority to make sure it runs before this
    @EventHandler(priority = EventPriority.HIGHEST) // runs after stat calculations
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer())) return;
        int shieldLeftOver = (int) damageShield(event.getPlayer(), event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player victim = (Player) event.getVictim();
        if (!RunicCore.getSpellAPI().isShielded(victim)) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer())) return;
        int shieldLeftOver = (int) damageShield(event.getPlayer(), event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    /**
     * Remove shield on character select
     */
    @EventHandler
    public void onQuit(CharacterSelectEvent event) {
        event.getPlayer().setAbsorptionAmount(0);
        RunicCore.getSpellAPI().getShieldedPlayers().remove(event.getPlayer().getUniqueId());
    }

    /**
     * Remove shield on logout
     */
    @EventHandler
    public void onQuit(CharacterQuitEvent event) {
        event.getPlayer().setAbsorptionAmount(0);
        RunicCore.getSpellAPI().getShieldedPlayers().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShield(SpellShieldEvent event) {
        if (event.isCancelled()) return;
        Player caster = event.getPlayer();
        Player recipient = event.getRecipient();
        int amount = event.getAmount();
        Map<UUID, Double> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        if (shieldedPlayers.containsKey(recipient.getUniqueId())) {
            Double previousAmount = shieldedPlayers.get(recipient.getUniqueId());
            shieldedPlayers.put(recipient.getUniqueId(), amount + previousAmount);
        } else {
            shieldedPlayers.put(recipient.getUniqueId(), (double) amount);
        }
        Bukkit.broadcastMessage("amount was: " + amount);
        Bukkit.broadcastMessage("shield is now: " + shieldedPlayers.get(recipient.getUniqueId()));
        double currentAmount = shieldedPlayers.get(recipient.getUniqueId());
        recipient.setAbsorptionAmount(currentAmount / HALF_HEART_AMOUNT);
        HologramUtil.createCombatHologram(Arrays.asList(caster, recipient), recipient.getEyeLocation(), ChatColor.YELLOW + "+" + amount + " ❤✦");
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 3, 0.35F, 0.35F, 0.35F, 0);
    }
}
