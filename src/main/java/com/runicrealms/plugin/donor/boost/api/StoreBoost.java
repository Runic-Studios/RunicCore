package com.runicrealms.plugin.donor.boost.api;

import java.util.Arrays;

public enum StoreBoost implements Boost {
    CRAFTING("crafting", "runic.boost.crafting", 45, 0.25, "Crafting", BoostExperienceType.CRAFTING),
    COMBAT("combat", "runic.boost.combat", 45, 0.5, "Combat", BoostExperienceType.COMBAT),
    GATHERING("gathering", "runic.boost.gathering", 45, 0.25, "Gathering", BoostExperienceType.GATHERING);

    private final String identifier;
    private final String permission;
    private final int duration; // in minutes
    private final double multiplier;
    private final String name;
    private final BoostExperienceType experienceType;

    StoreBoost(String identifier, String permission, int duration, double multiplier, String name, BoostExperienceType experienceType) {
        this.identifier = identifier;
        this.permission = permission;
        this.duration = duration;
        this.multiplier = multiplier;
        this.name = name;
        this.experienceType = experienceType;
    }

    public static StoreBoost getFromIdentifier(String identifier) {
        return Arrays.stream(values()).filter((boostType) -> boostType.identifier.equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getPermission() {
        return this.permission;
    }

    public int getDuration() {
        return this.duration;
    }

    public double getAdditionalMultiplier() {
        return this.multiplier;
    }

    public String getName() {
        return this.name;
    }

    public BoostExperienceType getExperienceType() {
        return this.experienceType;
    }

}
