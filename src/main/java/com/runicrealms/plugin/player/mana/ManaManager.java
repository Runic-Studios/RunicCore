package com.runicrealms.plugin.player.mana;

import com.runicrealms.plugin.events.ManaRegenEvent;
import com.runicrealms.plugin.item.GearScanner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.HashMap;
import java.util.UUID;

/**
 * Method to manage player mana. Stores max mana in the player data file,
 * and creates a HashMap to store all current player manas.
 * @author Skyfallin_
 */
public class ManaManager implements Listener {

    private static HashMap<UUID, Integer> currentPlayerManas;
    private static final int BASE_MANA = 100;
    private static final int MANA_REGEN_AMT = 5;
    private static final long MANA_REGEN_PERIOD = (long) 5; // seconds

    private static final int ARCHER_MANA_LV = 1;
    private static final int CLERIC_MANA_LV = 2;
    private static final int MAGE_MANA_LV = 3;
    private static final int ROGUE_MANA_LV = 1;
    private static final int WARRIOR_MANA_LV = 1;

    // constructor
    public ManaManager() {
        currentPlayerManas = new HashMap<>();
        this.startRegenTask();
    }

    private void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regenMana();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, MANA_REGEN_PERIOD *20);
    }

    private void regenMana() {

        for (Player online : Bukkit.getOnlinePlayers()) {

            // ensure they have loaded a character
            if (!RunicCore.getCacheManager().hasCacheLoaded(online.getUniqueId())) continue;

            int mana;

            if (currentPlayerManas.containsKey(online.getUniqueId())) {
                mana = currentPlayerManas.get(online.getUniqueId());
            } else {
                mana = getBaseMana() + getManaPerLv(online) + GearScanner.getManaBoost(online);
            }

            int maxMana = RunicCore.getCacheManager().getPlayerCache(online.getUniqueId()).getMaxMana();
            if (mana >= maxMana) continue;

            ManaRegenEvent event = new ManaRegenEvent(online, MANA_REGEN_AMT);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (mana + event.getAmount() >= maxMana) {
                    currentPlayerManas.put(online.getUniqueId(), maxMana);
                } else {
                    currentPlayerManas.put(online.getUniqueId(), mana + event.getAmount());
                }
                RunicCore.getScoreboardHandler().updateSideInfo(online);
            }
        }
    }

    public HashMap<UUID, Integer> getCurrentManaList() {
        return currentPlayerManas;
    }
    public int getBaseMana() {
        return BASE_MANA;
    }

    public int getManaRegenAmt() {
        return MANA_REGEN_AMT;
    }

    public static int getArcherManaLv() {
        return ARCHER_MANA_LV;
    }

    public static int getClericManaLv() {
        return CLERIC_MANA_LV;
    }

    public static int getMageManaLv() {
        return MAGE_MANA_LV;
    }

    public static int getRogueManaLv() {
        return ROGUE_MANA_LV;
    }

    public static int getWarriorManaLv() {
        return WARRIOR_MANA_LV;
    }

    public static int getManaPerLv(Player pl) {
        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        if (className == null) return 0;

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

    public void addMana(Player pl, int amt, boolean gemBoosted) {

        if (gemBoosted) {
            int boost = GearScanner.getHealingBoost(pl);
            amt = amt + boost;
        }

        int mana = currentPlayerManas.get(pl.getUniqueId());
        int maxMana = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getMaxMana();

        if (mana < maxMana) {

            if (mana + amt >= maxMana) {
                currentPlayerManas.put(pl.getUniqueId(), maxMana);
            } else {
                currentPlayerManas.put(pl.getUniqueId(), mana + amt);
            }
            RunicCore.getScoreboardHandler().updateSideInfo(pl);
        }
    }

    public void subtractMana(Player pl, int amt) {

        int mana = currentPlayerManas.get(pl.getUniqueId());

        if (mana <= 0) return;

        currentPlayerManas.put(pl.getUniqueId(), mana - amt);
        RunicCore.getScoreboardHandler().updateSideInfo(pl);
    }
 }
