package com.runicrealms.plugin.player;

public enum CombatType {

    MOB(CombatManager.COMBAT_DURATION_MOBS),
    PLAYER(CombatManager.COMBAT_DURATION_PLAYERS);

    private final double cooldown;

    CombatType(double cooldown) {
        this.cooldown = cooldown;
    }

    public double getCooldown() {
        return cooldown;
    }

}