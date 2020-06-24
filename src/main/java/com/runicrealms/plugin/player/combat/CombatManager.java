package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class manages combat against mobs and players and handles each differently,
 * which is why we need both a HashMap and a List.
 */
public class CombatManager implements Listener {

    private final HashMap<UUID, Long> playersInCombat;
    private final HashMap<UUID, Double> shieldedPlayers;
    private final Set<UUID> pvpers = new HashSet<>();
    private final RunicCore plugin = RunicCore.getInstance();
    private static final double COMBAT_DURATION = 10;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.shieldedPlayers = new HashMap<>();
        this.startCombatTask();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    // todo: this interacts in a weird way with the shield stat.
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
    public void onSpellDamage(SpellDamageEvent e) {
        if (e.getEntity() instanceof Player
                && (shieldedPlayers.containsKey((e.getEntity().getUniqueId())))) {
            e.setAmount(shieldDamage((Player) e.getEntity(),
                    e.getAmount(),
                    shieldedPlayers.get(e.getEntity().getUniqueId())));
        }
    }

    @EventHandler
    public void onWeapDamage(WeaponDamageEvent e) {
        if (e.getEntity() instanceof Player
                && (shieldedPlayers.containsKey((e.getEntity().getUniqueId())))) {
            e.setAmount(shieldDamage((Player) e.getEntity(),
                    e.getAmount(),
                    shieldedPlayers.get(e.getEntity().getUniqueId())));
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

    // starts the repeating task to manage pve/pvp timers
    private void startCombatTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player online : RunicCore.getCacheManager().getLoadedPlayers()) {
                    if(playersInCombat.containsKey(online.getUniqueId())) {
                        if (System.currentTimeMillis() - playersInCombat.get(online.getUniqueId()) >= (COMBAT_DURATION*1000)) {
                            playersInCombat.remove(online.getUniqueId());
                            pvpers.remove(online.getUniqueId());
                            online.sendMessage(ChatColor.GREEN + "You have left combat!");
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 20);
    }

    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
    }
    public HashMap<UUID, Double> getShieldedPlayers() {
        return this.shieldedPlayers;
    }
    public Set<UUID> getPvPers() {
        return this.pvpers;
    }

    public void addPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if (!playersInCombat.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        playersInCombat.put(uuid, System.currentTimeMillis());
    }

    /**
     * Used on death!
     */
    public void removePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        player.sendMessage(ChatColor.GREEN + "You have left combat!");
        playersInCombat.remove(uuid);
    }
}
