package com.runicrealms.plugin.luckperms;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.api.LuckPermsAPI;
import com.runicrealms.plugin.common.api.LuckPermsData;
import com.runicrealms.plugin.common.api.LuckPermsPayload;
import com.runicrealms.plugin.common.util.LazyField;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Important note about the LuckPermsManager: This only caches server-specific metadata values!
 * Any global values are completely ignored.
 */
public class LuckPermsManager implements LuckPermsAPI, Listener {

    private final static LazyField<ImmutableContextSet> serverContextSet = new LazyField<>(() -> {
        String serverType = LuckPermsProvider.get().getContextManager().getStaticContext()
                .toSet()
                .stream()
                .filter(context -> context.getKey().equals("server"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("FATAL: This instance does not have a LP server context set! NO LUCK PERMS DATA IS BEING SAVED!"))
                .getValue();
        return ImmutableContextSet.of("server", serverType);
    });

    // Stupid method of determining if a node is global: check it exists in server context, but not in this "impossible" context
    private final static LazyField<ImmutableContextSet> globalOnlyContextSet = new LazyField<>(() -> {
        return ImmutableContextSet.of("server", "aowieuibweifd");  // just don't name a server this please
    });

    private final Map<UUID, UserPayloadManager> payloadManagers = new HashMap<>();

    public LuckPermsManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!payloadManagers.containsKey(event.getPlayer().getUniqueId()))
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
        if (!payloadManagers.containsKey(owner))
            payloadManagers.put(owner, new UserPayloadManager(owner));
        return payloadManagers.get(owner).loadData(ignoreCache);
    }

    @Override
    public LuckPermsPayload createPayload(final UUID owner, final Consumer<LuckPermsData> writeConsumer) {
        return new LuckPermsPayload() {
            @Override
            public void saveToData(LuckPermsData data) {
                writeConsumer.accept(data);
            }

            @Override
            public UUID owner() {
                return owner;
            }
        };
    }

    @Override
    public ContextSet getServerSpecificContext() {
        return serverContextSet.get();
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
            LuckPermsProvider.get().getUserManager().loadUser(owner).thenAccept(user -> Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                // Convert loaded luckperms metadata into runic interface

                // Stupid method don't ask
                CachedMetaData serverMeta = user.getCachedData().getMetaData(QueryOptions.contextual(serverContextSet.get()));
                CachedMetaData globalOnlyMeta = user.getCachedData().getMetaData(QueryOptions.contextual(globalOnlyContextSet.get()));

                Map<String, String> loadedData = new HashMap<>();
                for (String key : serverMeta.getMeta().keySet()) {
                    if (globalOnlyMeta.getMetaValue(key) == null) loadedData.put(key, serverMeta.getMetaValue(key));
                }

                LuckPermsData data = new UserLuckPermsMetaData(loadedData);
                cachedData.set(data); // Save cached data
                future.complete(data); // Complete the future
            }));

            // Modify the default behavior of not printing exceptions
            future.exceptionally(exception -> {
                exception.printStackTrace();
                return null;
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

                LuckPermsProvider.get().getUserManager().loadUser(owner).thenAccept(user -> {
                    try {
                        // Clear the metadata keys that we are overwriting
                        user.data().clear(serverContextSet.get(), NodeType.META.predicate(metaNode -> data.containsKey(metaNode.getKey())));

                        // Write each metadata key
                        for (String key : data.getKeys()) {
                            user.data().add(MetaNode.builder(key, data.get(key).toString()).context(serverContextSet.get()).build());
                        }

                        // Save
                        LuckPermsProvider.get().getUserManager().saveUser(user).thenAccept($ -> future.complete(null));

                    } catch (Exception exception) {
                        Bukkit.getLogger().log(Level.SEVERE, "ERROR: Could not save luckperms payload");
                        exception.printStackTrace();
                        future.complete(null); // Make sure we complete the future
                    }
                });
            }, executor -> Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), executor));

            // Modify the default behavior of not printing exceptions
            future.exceptionally(exception -> {
                exception.printStackTrace();
                return null;
            });

            return future;
        }

        private void queueSave(final LuckPermsPayload payload, final boolean ignoreCache) {
            synchronized (this) {
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