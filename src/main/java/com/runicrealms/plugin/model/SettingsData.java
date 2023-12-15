package com.runicrealms.plugin.model;

import com.runicrealms.plugin.common.RunicCommon;

import java.util.UUID;

public class SettingsData {
    private final UUID owner;
    private boolean castMenuEnabled = true;
    private boolean openRunestoneInCombat = true;

    private String spellSlotOneDisplay = "1";
    private String spellSlotFourDisplay = "F";

    /**
     * Build the player's settings data from redis, then add to memory
     */
    public SettingsData(UUID owner) {
        RunicCommon.getLuckPermsAPI().retrieveData(owner).then(data -> {
            if (data.containsKey("settings.cast-menu"))
                castMenuEnabled = data.getBoolean("settings.cast-menu");
            if (data.containsKey("settings.open-runestone"))
                openRunestoneInCombat = data.getBoolean("settings.open-runestone");
            if (data.containsKey("settings.spell-one"))
                spellSlotOneDisplay = data.getString("settings.spell-one");
            if (data.containsKey("settings.spell-four"))
                spellSlotFourDisplay = data.getString("settings.spell-four");
        });
        this.owner = owner;
    }

    public void save() {
        RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(owner, (data) -> {
            data.set("settings.cast-menu", castMenuEnabled);
            data.set("settings.open-runestone", openRunestoneInCombat);
            data.set("settings.spell-one", spellSlotOneDisplay);
            data.set("settings.spell-four", spellSlotFourDisplay);
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

    public String getSpellSlotOneDisplay() {
        return spellSlotOneDisplay;
    }

    public void setSpellSlotOneDisplay(String spellSlotOneDisplay) {
        this.spellSlotOneDisplay = spellSlotOneDisplay;
        save();
    }

    public String getSpellSlotFourDisplay() {
        return spellSlotFourDisplay;
    }

    public void setSpellSlotFourDisplay(String spellSlotFourDisplay) {
        this.spellSlotFourDisplay = spellSlotFourDisplay;
        save();
    }
}
