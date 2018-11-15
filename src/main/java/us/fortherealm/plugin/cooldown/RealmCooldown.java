package us.fortherealm.plugin.cooldown;

import org.bukkit.entity.Player;

public class RealmCooldown {

    /*

    Realm Cooldown should be used by any class that contains a cooldown.
    It sorts, messages, and updates all cool downs. Feel free to extend
    it to insert your own functionality, but first make sure that you're
    not reinventing the wheel.

     */

    private long lastCast;
    private double cooldown;

    public RealmCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public void displayCooldown(Player player, String thingOnCooldown) {
        player.sendMessage(thingOnCooldown + "is on cooldown for " + (int) (getCurrentCooldown() / 1000) + " more seconds");
    }

    public boolean isOnCooldown() {
        return getCurrentCooldown() != 0;
    }

    public double getCooldown() {
        return cooldown;
    }

    public double getCurrentCooldown() {
        return Math.max(0, cooldown*1000 - (System.currentTimeMillis() - lastCast));
    }

    public double getTotalCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public long getLastCast() {
        return lastCast;
    }

    public void setLastCast(long lastCast) {
        this.lastCast = lastCast;
    }

}
