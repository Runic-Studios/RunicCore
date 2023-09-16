package com.runicrealms.plugin.spellapi;

public interface SpellEffect {

    /*
        private final String name;
    private final String description;
    private final boolean buff;
    private final String message;
    private final Sound sound;
     */
    // TODO: COMMENTS


    String getName();

    boolean isBuff();

    long getStartTime();

    double getDuration();
}
