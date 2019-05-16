package com.runicrealms.plugin.player;

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
    private static final int manaPerLevel = 1;
    private static final int manaRegenAmt = 5;
    private static final long manaRegenTime = (long) 5; // seconds

    // constructor
    public ManaManager() {
        currentPlayerManas = new HashMap<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            int maxMana = RunicCore.getInstance().getConfig().getInt(online.getUniqueId() + ".info.maxMana");
            currentPlayerManas.put(online.getUniqueId(), maxMana);
        }
        this.startRegenTask();
    }

    private void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regenMana();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, manaRegenTime*20);
    }

    private void regenMana() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            int mana = currentPlayerManas.get(online.getUniqueId());
            int maxMana = RunicCore.getInstance().getConfig().getInt(online.getUniqueId() + ".info.maxMana");
            if (mana >= maxMana) continue;

            if (mana+manaRegenAmt >= maxMana) {
                currentPlayerManas.put(online.getUniqueId(), maxMana);
            } else {
                currentPlayerManas.put(online.getUniqueId(), mana + manaRegenAmt);
            }
            RunicCore.getScoreboardHandler().updateSideInfo(online);
        }
    }

    public HashMap<UUID, Integer> getCurrentManaList() { return currentPlayerManas; }
    public int getManaPerLevel() { return manaPerLevel; }
 }
