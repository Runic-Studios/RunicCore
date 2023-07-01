package com.runicrealms.plugin.loot;

import com.runicrealms.plugin.RunicCore;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TimedLootChest extends LootChest {

    private final int duration; // in seconds
    private final Location hologramLocation;
    private final BiConsumer<Hologram, Integer> hologramEditor; // Integer is remaining duration in seconds
    private final Map<UUID, Runnable> finishTasks = new ConcurrentHashMap<>();

    public TimedLootChest(
            LootChestPosition position,
            LootChestTemplate lootChestTemplate,
            LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            String inventoryTitle,
            int duration,
            Location hologramLocation,
            BiConsumer<Hologram, Integer> hologramEditor) {
        super(position, lootChestTemplate, conditions, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle);
        this.duration = duration;
        this.hologramLocation = hologramLocation;
        this.hologramEditor = hologramEditor;
    }

    /**
     * WARNING: this method should not be called outside the LootManager and ClientLootManager classes!
     */
    public void beginDisplay(Player player, Runnable onFinish) {
        finishTasks.put(player.getUniqueId(), onFinish);
        AtomicInteger counter = new AtomicInteger(this.duration);
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(hologramLocation);
        showToPlayer(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                hologramEditor.accept(hologram, counter.get());
                counter.decrementAndGet();
                if (counter.get() <= 0) {
                    this.cancel();
                    hideFromPlayer(player);
                    Runnable finish = finishTasks.remove(player.getUniqueId());
                    if (finish != null) finish.run();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    @Override
    protected LootChestInventory generateInventory(Player player) {
        LootChestInventory inventory = super.generateInventory(player);
        inventory.onClose(target -> {
            hideFromPlayer(target);
            Runnable finish = finishTasks.remove(target.getUniqueId());
            if (finish != null) finish.run();
        });
        return inventory;
    }

    @Override
    public boolean shouldUpdateDisplay() {
        return false;
    }

}
