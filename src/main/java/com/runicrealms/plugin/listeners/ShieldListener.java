package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
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
    private static final int SHIELD_EXPIRE_TIME = 5; // Seconds

    /**
     * Running async task to expire shields after expire time
     */
    public ShieldListener() {
//        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
//            for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {
//                if (!RunicCore.getSpellAPI().isShielded(uuid)) continue;
//                long lastShieldTime = RunicCore.getSpellAPI().getShieldedPlayers().get(uuid).second;
//                if (System.currentTimeMillis() - lastShieldTime > SHIELD_EXPIRE_TIME * 1000) {
//                    Player player = Bukkit.getPlayer(uuid);
//                    if (player != null) {
//                        removeShield(player, RunicCore.getSpellAPI().getShieldedPlayers());
//                    }
//                }
//            }
//        }, 0, 5L);
    }

    /**
     * Damages a player's shield. If there's still shield left, updates the displayed yellow hearts.
     * Otherwise, returns the damage the player should take
     *
     * @param player who was damaged
     * @param damage the player received
     * @return the damage left over after the shield
     */
    private double damageShield(Player player, double damage) {
        Map<UUID, Pair<Double, Long>> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        double shield = shieldedPlayers.get(player.getUniqueId()).first;
        double shieldLeftOver = shield - damage;
        if (shieldLeftOver > 0) {
            player.setAbsorptionAmount(shieldLeftOver / HALF_HEART_AMOUNT);
            shieldedPlayers.put(player.getUniqueId(), Pair.pair(shieldLeftOver, System.currentTimeMillis()));
        } else if (shieldLeftOver <= 0) {
            // Shield was broken and there's leftover damage
            removeShield(player, shieldedPlayers);
        }
        return shieldLeftOver;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs after stat calculations
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer().getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(event.getPlayer(), event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMobDamage(MobDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player victim = (Player) event.getVictim();
        if (!RunicCore.getSpellAPI().isShielded(victim.getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(victim, event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!RunicCore.getSpellAPI().isShielded(event.getPlayer().getUniqueId())) return;
        int shieldLeftOver = (int) damageShield(event.getPlayer(), event.getAmount());
        event.setAmount(Math.min(shieldLeftOver, 0)); // Deal leftover damage if shield is negative
    }

    /**
     * Remove shield on character select
     */
    @EventHandler
    public void onQuit(CharacterSelectEvent event) {
        removeShield(event.getPlayer(), RunicCore.getSpellAPI().getShieldedPlayers());
    }

    /**
     * Remove shield on logout
     */
    @EventHandler
    public void onQuit(CharacterQuitEvent event) {
        removeShield(event.getPlayer(), RunicCore.getSpellAPI().getShieldedPlayers());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onShield(SpellShieldEvent event) {
        if (event.isCancelled()) return;
        Player caster = event.getPlayer();
        Player recipient = event.getRecipient();
        int amount = event.getAmount();
        Map<UUID, Pair<Double, Long>> shieldedPlayers = RunicCore.getSpellAPI().getShieldedPlayers();
        if (shieldedPlayers.containsKey(recipient.getUniqueId())) {
            double previousAmount = shieldedPlayers.get(recipient.getUniqueId()).first;
            Bukkit.broadcastMessage(ChatColor.YELLOW + "previous amount was " + previousAmount);
            shieldedPlayers.put(recipient.getUniqueId(), Pair.pair((amount + previousAmount), System.currentTimeMillis()));
        } else {
            shieldedPlayers.put(recipient.getUniqueId(), Pair.pair((double) amount, System.currentTimeMillis()));
        }
        Bukkit.broadcastMessage("amount was: " + amount);
        Bukkit.broadcastMessage("shield is now: " + shieldedPlayers.get(recipient.getUniqueId()).first);
        double currentAmount = shieldedPlayers.get(recipient.getUniqueId()).first;
        recipient.setAbsorptionAmount(currentAmount / HALF_HEART_AMOUNT);
        HologramUtil.createCombatHologram(Arrays.asList(caster, recipient), recipient.getEyeLocation(), ChatColor.YELLOW + "+" + amount + " ❤✦");
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 3, 0.35F, 0.35F, 0.35F, 0);
    }

    /**
     * Removes a player's shield
     *
     * @param player          to remove shield from
     * @param shieldedPlayers the map of all shields
     */
    private void removeShield(Player player, Map<UUID, Pair<Double, Long>> shieldedPlayers) {
        player.setAbsorptionAmount(0);
        shieldedPlayers.remove(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 0.5f, 1.0f);
    }
}
