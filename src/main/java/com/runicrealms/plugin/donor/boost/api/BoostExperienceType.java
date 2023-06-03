package com.runicrealms.plugin.donor.boost.api;

import java.util.Arrays;

public enum BoostExperienceType {

    CRAFTING("crafting"),
    COMBAT("combat"),
    GATHERING("gathering");

    private final String identifier;

    BoostExperienceType(String identifier) {
        this.identifier = identifier;
    }

    public static BoostExperienceType getFromIdentifier(String identifier) {
        return Arrays.stream(values()).filter((experienceType) -> experienceType.identifier.equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
