package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Shield {
    private final Set<UUID> sources;
    private long startTime;
    private double amount;

    /**
     * pair of double (the shield amount) and long (the time the shield was applied)
     *
     * @param amount     of the shield
     * @param startTime  when the shield was last applied
     * @param sourceUuid of the caster who applied the shield
     */
    public Shield(double amount, long startTime, UUID sourceUuid) {
        this.amount = amount;
        this.startTime = startTime;
        this.sources = new HashSet<UUID>() {{
            add(sourceUuid);
        }};
    }

    /**
     * @param sourceUuid uuid of player who contributed to this shield
     */
    public void addSource(UUID sourceUuid) {
        this.sources.add(sourceUuid);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Set<UUID> getSources() {
        return sources;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
