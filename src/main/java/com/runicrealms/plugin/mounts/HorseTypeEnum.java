package com.runicrealms.plugin.mounts;

public enum HorseTypeEnum {

    NORMAL(0.25f),
    EPIC(0.5f);

    private float speed;
    HorseTypeEnum(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return this.speed;
    }
}
