package com.runicrealms.plugin.mounts;

public enum HorseType {

    NORMAL(0.25f),
    EPIC(0.5f);

    private float speed;
    HorseType(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return this.speed;
    }
}
