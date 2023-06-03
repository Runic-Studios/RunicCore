package com.runicrealms.plugin.donor.boost.api;

public interface Boost {

    int getDuration();

    double getAdditionalMultiplier();

    String getName();

    BoostExperienceType getExperienceType();

}
