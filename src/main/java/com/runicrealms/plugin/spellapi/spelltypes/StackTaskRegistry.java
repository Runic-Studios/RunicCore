package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class StackTaskRegistry {
    private final List<StackTask> stackTasks = new ArrayList<>();
    private BukkitTask updateTask;

    public StackTaskRegistry() {
        startUpdating();
    }

    public void registerStackTask(StackTask stackTask) {
        stackTasks.add(stackTask);
    }

    public void unregisterStackTask(StackTask stackTask) {
        stackTasks.remove(stackTask);
    }

    private void startUpdating() {
        updateTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> stackTasks.removeIf(stackTask -> {
            Player player = stackTask.getCaster();
            if (!player.isOnline()) {
                stackTask.getBukkitTask().cancel();
                return true;
            }
            return false;
        }), 0L, 20L);
    }

    public void stopUpdating() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

}
