package com.runicrealms.plugin.mounts;

public enum HorseTypeEnum {

    NORMAL("Normal", 0.25f),
    EPIC("Epic", 0.5f);

    String value;
    private float speed;

    HorseTypeEnum(String value, float speed) {
        this.value = value;
        this.speed = speed;
    }

    public float getSpeed() {
        return this.speed;
    }
}
