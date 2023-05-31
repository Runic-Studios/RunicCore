package com.runicrealms.plugin.playerqueue;

import java.util.UUID;

public class QueuedPlayer {

    private final UUID uuid;
    private final boolean priority;
    private long lastJoin;

    public QueuedPlayer(UUID uuid, boolean priority, long lastJoin) {
        this.uuid = uuid;
        this.priority = priority;
        this.lastJoin = lastJoin;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean hasPriority() {
        return this.priority;
    }

    public long getLastJoin() {
        return this.lastJoin;
    }

    public void updateLastJoin() {
        lastJoin = System.currentTimeMillis();
    }

}
