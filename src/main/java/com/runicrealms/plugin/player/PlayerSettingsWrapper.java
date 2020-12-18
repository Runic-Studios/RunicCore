package com.runicrealms.plugin.player;

import org.bukkit.entity.Player;

public class PlayerSettingsWrapper {

    private final Player player;
    private boolean displaySpellUI;

    public PlayerSettingsWrapper(Player player, boolean displaySpellUI) {
        this.player = player;
        this.displaySpellUI = displaySpellUI;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isDisplaySpellUI() {
        return displaySpellUI;
    }

    public void setDisplaySpellUI(boolean displaySpellUI) {
        this.displaySpellUI = displaySpellUI;
    }
}
