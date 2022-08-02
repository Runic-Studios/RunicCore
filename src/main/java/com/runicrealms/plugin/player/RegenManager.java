package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.HealthRegenEvent;
import com.runicrealms.plugin.events.ManaRegenEvent;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.plugin.redis.RedisField;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

/**
 * CLass to manage player health and mana. Stores max mana in the player data file,
 * and creates a HashMap to store all current player mana values.
 *
 * @author Skyfallin_
 */
public class RegenManager implements Listener {

    private static final int HEALTH_REGEN_BASE_VALUE = 5;
    private static final double HEALTH_REGEN_LEVEL_MULTIPLIER = 0.15;
    private static final int OOC_MULTIPLIER = 4; // out-of-combat
    private static final int REGEN_PERIOD = 4; // seconds

    private static final int BASE_MANA = 150;
    private static final int MANA_REGEN_AMT = 5;

    private static final double ARCHER_MANA_LV = 1.75;
    private static final double CLERIC_MANA_LV = 2.25;
    private static final double MAGE_MANA_LV = 2.75;
    private static final double ROGUE_MANA_LV = 1.5;
    private static final double WARRIOR_MANA_LV = 1.5;

    private final HashMap<UUID, Integer> currentPlayerManaValues = new HashMap<>();

    public RegenManager() {
        // regen health async because of costly location checks (for checking for safe zones)
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), this::regenHealth, 0, REGEN_PERIOD * 20L);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), this::regenMana, 0, REGEN_PERIOD * 20L);
    }

    /**
     * Task to regen health with appropriate modifiers
     */
    private void regenHealth() {
        for (UUID loaded : RunicCoreAPI.getLoadedCharacters()) {
            Player online = Bukkit.getPlayer(loaded);
            if (online == null) continue;
            int regenAmount = (int) (HEALTH_REGEN_BASE_VALUE + (HEALTH_REGEN_LEVEL_MULTIPLIER * online.getLevel()));
            if (!RunicCoreAPI.isInCombat(online)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    HealthRegenEvent event = new HealthRegenEvent(online, regenAmount * OOC_MULTIPLIER);
                    Bukkit.getPluginManager().callEvent(event);
                });
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                    HealthRegenEvent event = new HealthRegenEvent(online, regenAmount);
                    Bukkit.getPluginManager().callEvent(event);
                });
            }
        }
    }

    /**
     * Periodic task to regenerate mana for all online players
     */
    private void regenMana() {
        for (UUID loaded : RunicCore.getDatabaseManager().getLoadedCharacters()) {
            Player online = Bukkit.getPlayer(loaded);
            if (online == null) continue;

            int mana;

            if (currentPlayerManaValues.containsKey(online.getUniqueId()))
                mana = currentPlayerManaValues.get(online.getUniqueId());
            else
                mana = (int) (getBaseMana() + getManaPerLv(online));

            int maxMana = RunicCoreAPI.calculateMaxMana(online);
            if (mana >= maxMana) continue;

            int regenAmt = MANA_REGEN_AMT;
            if (RunicCore.getCombatManager().getPlayersInCombat().get(online.getUniqueId()) == null)
                regenAmt *= 5; // players regen a lot out of combat

            ManaRegenEvent event = new ManaRegenEvent(online, regenAmt);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                currentPlayerManaValues.put(online.getUniqueId(), Math.min(mana + event.getAmount(), maxMana));
            }
        }
    }

    public HashMap<UUID, Integer> getCurrentManaList() {
        return currentPlayerManaValues;
    }

    public static int getBaseMana() {
        return BASE_MANA;
    }

    public static int getManaRegenAmt() {
        return MANA_REGEN_AMT;
    }

    /**
     * Determines the amount of mana to award per level to the given player based on class
     *
     * @param player to calculate mana for
     * @return the mana per level
     */
    public double getManaPerLv(Player player) {
        String className = RunicCoreAPI.getRedisValue(player, RedisField.CLASS_TYPE.getField());
        if (className.equals("")) return 0;
        switch (className.toLowerCase()) {
            case "archer":
                return ARCHER_MANA_LV;
            case "cleric":
                return CLERIC_MANA_LV;
            case "mage":
                return MAGE_MANA_LV;
            case "rogue":
                return ROGUE_MANA_LV;
            case "warrior":
                return WARRIOR_MANA_LV;
        }
        return 0;
    }

    /**
     * Adds mana to the current pool for the given player. Cannot add above max mana pool
     *
     * @param player to receive mana
     * @param amount of mana to receive
     */
    public void addMana(Player player, int amount) {
        int mana = currentPlayerManaValues.get(player.getUniqueId());
        int maxMana = ManaListener.calculateMaxMana(player);
        if (mana < maxMana)
            currentPlayerManaValues.put(player.getUniqueId(), Math.min(mana + amount, maxMana));
    }

    /**
     * Removes mana to the current pool for the given player. Cannot remove below 0
     *
     * @param player to lose mana
     * @param amount of mana to lose
     */
    public void subtractMana(Player player, int amount) {
        int mana = currentPlayerManaValues.get(player.getUniqueId());
        if (mana <= 0) return;
        currentPlayerManaValues.put(player.getUniqueId(), Math.max((mana - amount), 0));
    }
}
