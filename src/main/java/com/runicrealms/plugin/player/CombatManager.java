package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class manages combat against mobs and players and handles each differently,
 * which is why we need both a HashMap and a List.
 */
public class CombatManager implements Listener {

    private static final double COMBAT_DURATION = 15;
    private final HashMap<UUID, Long> playersInCombat;
    private final HashMap<UUID, Double> shieldedPlayers;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.shieldedPlayers = new HashMap<>();
        this.startCombatTask();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Adds a player to the combat set, sends them a message
     *
     * @param uuid of the player to add
     */
    public void addPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if (!playersInCombat.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        playersInCombat.put(uuid, System.currentTimeMillis());
    }

    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
    }

    public HashMap<UUID, Double> getShieldedPlayers() {
        return this.shieldedPlayers;
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent e) {
        if (e.getVictim() instanceof Player
                && (shieldedPlayers.containsKey((e.getVictim().getUniqueId())))) {
            e.setAmount(shieldDamage((Player) e.getVictim(),
                    e.getAmount(),
                    shieldedPlayers.get(e.getVictim().getUniqueId())));
        }
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        if (e.getVictim() instanceof Player
                && (shieldedPlayers.containsKey((e.getVictim().getUniqueId())))) {
            e.setAmount(shieldDamage((Player) e.getVictim(),
                    e.getAmount(),
                    shieldedPlayers.get(e.getVictim().getUniqueId())));
        }
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        if (e.getVictim() instanceof Player
                && (shieldedPlayers.containsKey((e.getVictim().getUniqueId())))) {
            e.setAmount(shieldDamage((Player) e.getVictim(),
                    e.getAmount(),
                    shieldedPlayers.get(e.getVictim().getUniqueId())));
        }
    }

    private int shieldDamage(Player pl, int eventAmount, double shieldAmount) {
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.5f);
        if (shieldAmount < eventAmount)
            HologramUtil.createShieldDamageHologram(pl, pl.getLocation().clone().add(0, 1.5, 0), shieldAmount);
        else
            HologramUtil.createShieldDamageHologram(pl, pl.getLocation().clone().add(0, 1.5, 0), eventAmount);
        int temp = eventAmount;
        eventAmount -= shieldAmount;
        shieldAmount -= temp;
        if (shieldAmount > 0)
            shieldedPlayers.put(pl.getUniqueId(), shieldAmount);
        else
            shieldedPlayers.remove(pl.getUniqueId());
        return Math.max(eventAmount, 0);
    }

    /**
     * starts the repeating task to manage pve/pvp timers
     */
    private void startCombatTask() {
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (UUID uuid : RunicCoreAPI.getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                if (playersInCombat.containsKey(uuid)) {
                    if (System.currentTimeMillis() - playersInCombat.get(uuid) >= (COMBAT_DURATION * 1000)) {
                        LeaveCombatEvent leaveCombatEvent = new LeaveCombatEvent(player);
                        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
                    }
                }
            }
        }, 0, 20L);
    }
}
