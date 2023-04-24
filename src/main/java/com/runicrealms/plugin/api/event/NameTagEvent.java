package com.runicrealms.plugin.api.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when a player's name tag will be modified
 */
public class NameTagEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int slot;
    private ChatColor prefixColor;
    private ChatColor nameColor;
    private String nametag;
    private boolean isCancelled;

    /**
     * @param prefixColor the color of the class|level prefix
     * @param slot        of the character
     * @param nameColor   the color of the name itself
     * @param nametag     the contents of the tag (should include the player's name)
     */
    public NameTagEvent(Player player, int slot, ChatColor prefixColor, ChatColor nameColor, String nametag) {
        this.player = player;
        this.slot = slot;
        this.prefixColor = prefixColor;
        this.nameColor = nameColor;
        this.nametag = nametag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ChatColor getNameColor() {
        return nameColor;
    }

    public void setNameColor(ChatColor nameColor) {
        this.nameColor = nameColor;
    }

    public String getNametag() {
        return nametag;
    }

    public void setNametag(String nametag) {
        this.nametag = nametag;
    }

    public Player getPlayer() {
        return player;
    }

    public ChatColor getPrefixColor() {
        return prefixColor;
    }

    public void setPrefixColor(ChatColor prefixColor) {
        this.prefixColor = prefixColor;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
}
