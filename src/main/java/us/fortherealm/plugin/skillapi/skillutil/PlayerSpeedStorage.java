package us.fortherealm.plugin.skillapi.skillutil;

import org.bukkit.entity.Player;

public class PlayerSpeedStorage {

    private Player player;
    private float originalSpeed;
    private long expirationTime;

    public PlayerSpeedStorage(Player player, float originalSpeed, long expirationTime) {
        this.player = player;
        this.originalSpeed = originalSpeed;
        this.expirationTime = expirationTime;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public float getOriginalSpeed() {
        return originalSpeed;
    }

    public void setOriginalSpeed(float originalSpeed) {
        this.originalSpeed = originalSpeed;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
