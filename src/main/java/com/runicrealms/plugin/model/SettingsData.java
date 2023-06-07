package com.runicrealms.plugin.model;

import com.runicrealms.plugin.common.RunicCommon;

import java.util.UUID;

public class SettingsData {
    private final UUID owner;
    private boolean castMenuEnabled = true;
    private boolean openRunestoneInCombat = true;

    /**
     * Build the player's settings data from redis, then add to memory
     */
    public SettingsData(UUID owner) {
        RunicCommon.getLuckPermsAPI().retrieveData(owner).thenAcceptAsync(data -> {
            if (data.containsKey("settings.cast-menu")) castMenuEnabled = data.getBoolean("settings.cast-menu");
            if (data.containsKey("settings.open-runestone"))
                openRunestoneInCombat = data.getBoolean("settings.open-runestone");
        });
        this.owner = owner;
    }

    public void save() {
        RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(owner, (data) -> {
            data.set("settings.cast-menu", castMenuEnabled);
            data.set("settings.open-runestone", openRunestoneInCombat);
        }));
    }

    public boolean isCastMenuEnabled() {
        return castMenuEnabled;
    }

    public void setCastMenuEnabled(boolean castMenuEnabled) {
        this.castMenuEnabled = castMenuEnabled;
        save();
    }

    public boolean shouldOpenRunestoneInCombat() {
        return openRunestoneInCombat;
    }

    public void setOpenRunestoneInCombat(boolean openRunestoneInCombat) {
        this.openRunestoneInCombat = openRunestoneInCombat;
        save();
    }
}
