package com.runicrealms.plugin.luckperms;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.LuckPermsAPI;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class LuckPermsManager implements LuckPermsAPI, Listener {

    private final Map<UUID, UserPayloadManager> payloadManagers = new HashMap<>();

    public LuckPermsManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        payloadManagers.put(event.getPlayer().getUniqueId(), new UserPayloadManager(event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        payloadManagers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void savePayload(LuckPermsPayload payload) {
        savePayload(payload, false);
    }

    @Override
    public void savePayload(LuckPermsPayload payload, boolean ignoreCache) {
        payloadManagers.get(payload.owner()).queueSave(payload, ignoreCache);
    }

    @Override
    public CompletableFuture<LuckPermsData> retrieveData(UUID owner) {
        return retrieveData(owner, false);
    }

    @Override
    public CompletableFuture<LuckPermsData> retrieveData(UUID owner, boolean ignoreCache) {
        return payloadManagers.get(owner).loadData(ignoreCache);
    }

    private static class UserPayloadManager {

        private final ConcurrentLinkedQueue<LuckPermsPayload> queuedSaves = new ConcurrentLinkedQueue<>();
        private final UUID owner;
        private final AtomicReference<LuckPermsData> cachedData = new AtomicReference<>(null);
        private CompletableFuture<Void> currentFuture = CompletableFuture.completedFuture(null);

        private UserPayloadManager(UUID owner) {
            this.owner = owner;
        }

        private CompletableFuture<LuckPermsData> loadData(boolean ignoreCache) {
            // If we have cached the data, then return that immediately
            if (cachedData.get() != null && !ignoreCache) return CompletableFuture.completedFuture(cachedData.get());

            final CompletableFuture<LuckPermsData> future = new CompletableFuture<>();
            LuckPermsProvider.get().getUserManager().loadUser(owner).thenAcceptAsync(user -> {
                // Convert loaded luckperms metadata into runic interface
                LuckPermsData data = UserLuckPermsMetaData.loadFromCachedMetaData(user.getCachedData().getMetaData());
                cachedData.set(data); // Save cached data
                future.complete(data); // Complete the future
            });
            return future;
        }

        private CompletableFuture<Void> savePayload(final LuckPermsPayload payload, boolean ignoreCache) {

            // First load existing data before saving
            CompletableFuture<LuckPermsData> loadFuture;
            if (cachedData.get() != null && !ignoreCache) {
                loadFuture = CompletableFuture.completedFuture(cachedData.get());
            } else {
                loadFuture = loadData(ignoreCache);
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            // Begin save after loading data
            loadFuture.thenAcceptAsync(data -> {
                // Save our payload to existing data
                payload.saveToData(data);

                // Update the cache
                if (cachedData.get() != null) cachedData.get().add(data);

                LuckPermsProvider.get().getUserManager().loadUser(owner).thenAcceptAsync(user -> {
                    try {
                        // Clear the metadata keys that we are overwriting, if they exist
                        user.data().clear(NodeType.META.predicate(metaNode -> data.containsKey(metaNode.getMetaKey())
                                && !Objects.equals(data.get(metaNode.getMetaKey()).toString(), metaNode.getMetaValue())));
                        // Write each metadata key
                        for (String key : data.getKeys())
                            user.data().add(MetaNode.builder(key, data.get(key).toString()).build());
                        // Save
                        LuckPermsProvider.get().getUserManager().saveUser(user).thenAcceptAsync($ -> future.complete(null));
                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR: Could not save luckperms payload");
                        exception.printStackTrace();
                        future.complete(null); // Make sure we complete the future
                    }
                });
            });
            return future;
        }

        private void queueSave(final LuckPermsPayload payload, final boolean ignoreCache) {
            synchronized (UserPayloadManager.class) {
                queuedSaves.add(payload); // add payload to queue
                Runnable queuedRunnable = () -> {
                    LuckPermsPayload polledPayload = queuedSaves.poll();
                    if (polledPayload != null) currentFuture = savePayload(polledPayload, ignoreCache);
                };
                // If the current future has been complete, run a save immediately
                if (currentFuture.isDone()) currentFuture = CompletableFuture.runAsync(queuedRunnable);
                    // If the current future is still processing, chain it to the end of it
                else currentFuture = currentFuture.thenRunAsync(queuedRunnable);
            }
        }

    }

}
