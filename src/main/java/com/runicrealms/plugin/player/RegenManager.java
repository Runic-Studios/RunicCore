package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.HealthRegenEvent;
import com.runicrealms.plugin.events.ManaRegenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

/**
 * CLass to manage player health and mana. Stores max mana in the player data file,
 * and creates a HashMap to store all current player mana values.
 * @author Skyfallin_
 */
public class RegenManager implements Listener {

    private static final int HEALTH_REGEN_BASE_VALUE = 5;
    private static final double HEALTH_REGEN_LEVEL_MULTIPLIER = 0.15;
    private static final int OOC_MULTIPLIER = 4; // out-of-combat
    private static final int REGEN_PERIOD = 4; // seconds

    private static final int BASE_MANA = 100;
    private static final int MANA_REGEN_AMT = 5;

    private static final double ARCHER_MANA_LV = 1.75;
    private static final double CLERIC_MANA_LV = 2.25;
    private static final double MAGE_MANA_LV = 2.75;
    private static final double ROGUE_MANA_LV = 1.5;
    private static final double WARRIOR_MANA_LV = 1.5;

    private final HashMap<UUID, Integer> currentPlayerManaValues = new HashMap<>();

    public RegenManager() {
        // regen health async because of costly location checks
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(RunicCore.getInstance(), this::regenHealth, 0, REGEN_PERIOD * 20L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicCore.getInstance(), this::regenMana, 0, REGEN_PERIOD * 20L);
    }

    /**
     * Only regen health in safe zones, or small amount if player has invigorated buff
     */
    private void regenHealth() {
        for (Player online : RunicCore.getCacheManager().getLoadedPlayers()) {
            int regenAmount = (int) (HEALTH_REGEN_BASE_VALUE + (HEALTH_REGEN_LEVEL_MULTIPLIER * online.getLevel()));
            if (RunicCoreAPI.isSafezone(online.getLocation()) || !RunicCoreAPI.isInCombat(online)) {
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

    private void regenMana() {

        for (Player online : Bukkit.getOnlinePlayers()) {

            // ensure they have loaded a character
            if (!RunicCore.getCacheManager().hasCacheLoaded(online)) continue;

            int mana;

            if (currentPlayerManaValues.containsKey(online.getUniqueId()))
                mana = currentPlayerManaValues.get(online.getUniqueId());
            else
                mana = (int) (getBaseMana() + getManaPerLv(online));

            int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(online).getMaxMana();
            if (mana >= maxMana) continue;

            int regenAmt = MANA_REGEN_AMT; // todo: not sure where else this is called, but not needed? + GearScanner.getManaRegenBoost(online)
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

    public int getBaseMana() {
        return BASE_MANA;
    }

    public double getManaPerLv(Player pl) {

        if (RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName() == null)
            return 0;
        String className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();

        switch(className.toLowerCase()) {
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

    public void addMana(Player pl, int amt) {
        int mana = currentPlayerManaValues.get(pl.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(pl).getMaxMana();
        if (mana < maxMana)
            currentPlayerManaValues.put(pl.getUniqueId(), Math.min(mana + amt, maxMana));
    }

    public void subtractMana(Player pl, int amt) {
        int mana = currentPlayerManaValues.get(pl.getUniqueId());
        if (mana <= 0)
            return;
        currentPlayerManaValues.put(pl.getUniqueId(), Math.max((mana - amt), 0));
    }
 }
