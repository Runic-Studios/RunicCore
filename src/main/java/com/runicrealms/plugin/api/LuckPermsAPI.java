package com.runicrealms.plugin.api;

import com.runicrealms.plugin.luckperms.LuckPermsData;
import com.runicrealms.plugin.luckperms.LuckPermsPayload;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LuckPermsAPI {

    /**
     * Save a payload asynchronously to the luckperms database
     */
    void savePayload(LuckPermsPayload payload);

    /**
     * Save a payload asynchronously to the luckperms database.
     * If ignoreCache is true, forcibly reloads it from the DB, ignoring cached values.
     */
    void savePayload(LuckPermsPayload payload, boolean ignoreCache);

    /**
     * Retrieve all luckperms metadata for a given user.
     */
    CompletableFuture<LuckPermsData> retrieveData(UUID owner);

    /**
     * Retrieve all luckperms metadata for a given user.
     * If ignoreCache is true, forcibly reloads it from the DB, ignoring cached values.
     */
    CompletableFuture<LuckPermsData> retrieveData(UUID owner, boolean ignoreCache);

}
