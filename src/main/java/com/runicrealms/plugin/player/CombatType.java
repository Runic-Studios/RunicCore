package com.runicrealms.plugin.player;

public enum CombatType {

    MOB(10),
    PLAYER(30);

    private final double cooldown;

    CombatType(double cooldown) {
        this.cooldown = cooldown;
    }

    public double getCooldown() {
        return cooldown;
    }

}