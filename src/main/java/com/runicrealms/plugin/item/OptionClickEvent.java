package com.runicrealms.plugin.item;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OptionClickEvent {

    private InventoryClickEvent invClickEvent;
    private Player player;
    private int position;
    private String name;
    private boolean close;
    private boolean destroy;

    public OptionClickEvent(InventoryClickEvent invClickEvent, Player player, int position, String name) {
        this.invClickEvent = invClickEvent;
        this.player = player;
        this.position = position;
        this.name = name;
        this.close = true;
        this.destroy = false;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public InventoryClickEvent getSuper() { return invClickEvent; }

    public boolean willClose() {
        return close;
    }

    public boolean willDestroy() {
        return destroy;
    }

    public void setWillClose(boolean close) {
        this.close = close;
    }

    public void setWillDestroy(boolean destroy) {
        this.destroy = destroy;
    }
}