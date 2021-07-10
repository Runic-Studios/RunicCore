package com.runicrealms.plugin.player.mana;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.HealthRegenEvent;
import com.runicrealms.plugin.events.ManaRegenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

/**
 * CLass to manage player health and mana. Stores max mana in the player data file,
 * and creates a HashMap to store all current player manas.
 * @author Skyfallin_
 */
@SuppressWarnings("FieldCanBeLocal")
public class RegenManager implements Listener {

    private final int HEALTH_REGEN_AMT = 5;
    private final long REGEN_PERIOD = 4; // seconds

    private final HashMap<UUID, Integer> currentPlayerManas;
    private final int BASE_MANA = 100;
    private final int MANA_REGEN_AMT = 5;

    private final double ARCHER_MANA_LV = 1.75;
    private final double CLERIC_MANA_LV = 2.25;
    private final double MAGE_MANA_LV = 2.75;
    private final double ROGUE_MANA_LV = 1.5;
    private final double WARRIOR_MANA_LV = 1.5;

    // constructor
    public RegenManager() {
        currentPlayerManas = new HashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                regenHealth();
                regenMana();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, REGEN_PERIOD * 20);
    }

    private void regenHealth() {
        for (Player online : RunicCore.getCacheManager().getLoadedPlayers()) {
            int regenAmt = HEALTH_REGEN_AMT;
            if (RunicCore.getCombatManager().getPlayersInCombat().get(online.getUniqueId()) == null)
                regenAmt *= 5; // players regen a lot out of combat
            HealthRegenEvent event = new HealthRegenEvent(online, regenAmt);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    private void regenMana() {

        for (Player online : Bukkit.getOnlinePlayers()) {

            // ensure they have loaded a character
            if (!RunicCore.getCacheManager().hasCacheLoaded(online)) continue;

            int mana;

            if (currentPlayerManas.containsKey(online.getUniqueId()))
                mana = currentPlayerManas.get(online.getUniqueId());
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
                currentPlayerManas.put(online.getUniqueId(), Math.min(mana + event.getAmount(), maxMana));
            }
        }
    }

    public HashMap<UUID, Integer> getCurrentManaList() {
        return currentPlayerManas;
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
        int mana = currentPlayerManas.get(pl.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(pl).getMaxMana();
        if (mana < maxMana)
            currentPlayerManas.put(pl.getUniqueId(), Math.min(mana + amt, maxMana));
    }

    public void subtractMana(Player pl, int amt) {
        int mana = currentPlayerManas.get(pl.getUniqueId());
        if (mana <= 0)
            return;
        currentPlayerManas.put(pl.getUniqueId(), Math.max((mana - amt), 0));
    }
 }
