package com.runicrealms.plugin.item.GUIMenu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OptionClickEvent extends InventoryClickEvent {

    private Player player;
    private int position;
    private String name;
    private boolean close;
    private boolean destroy;

    public OptionClickEvent(InventoryClickEvent invClickEvent, Player player, int position, String name) {
        super(invClickEvent.getView(), invClickEvent.getSlotType(), position, invClickEvent.getClick(), invClickEvent.getAction());
        this.player = player;
        this.position = position;
        this.name = name;
        this.close = true;
        this.destroy = false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

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