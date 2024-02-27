package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModeledSpellManager implements Listener, ModeledSpellAPI {
    private final Map<UUID, ModeledSpell> activeModeledSpells = new ConcurrentHashMap<>();

    public ModeledSpellManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startCollisionCheckTask();
    }

    private void startCollisionCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeModeledSpells.forEach((uuid, modeledSpell) -> {
                    // Remove stand if duration has expired
                    if (System.currentTimeMillis() - modeledSpell.getStartTime() > (modeledSpell.getDuration() * 1000)) {
                        activeModeledSpells.remove(modeledSpell.getModeledEntity().getBase().getUUID());
                        modeledSpell.getModeledEntity().destroy();
                        return;
                    }

                    // Continuously teleport projectile modeled spell
//                    if (modeledSpell instanceof ModeledSpellAttached) {
//                        modeledSpell.getDummy().setLocation(modeledSpell.getPlayer().getLocation());
//                    }

                });
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 3L);
    }

    @Override
    public void addModeledSpellToManager(ModeledSpell modeledSpell) {
        this.activeModeledSpells.put(modeledSpell.getModeledEntity().getBase().getUUID(), modeledSpell);
    }
}
