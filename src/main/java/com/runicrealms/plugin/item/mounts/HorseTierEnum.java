package com.runicrealms.plugin.item.mounts;

public enum HorseTierEnum {

    NORMAL(0.25f, 0.5f), // +25%
    EPIC(0.3f, 0.625f), // +50%
    LEGENDARY(0.4f, 0.75f); // +100%

    private final float speed;
    private final float jumpSpeed;

    HorseTierEnum(float speed, float jumpSpeed) {
        this.speed = speed;
        this.jumpSpeed = jumpSpeed;
    }

    public float getSpeed() {
        return speed;
    }

    public float getJumpSpeed() {
        return jumpSpeed;
    }
}
