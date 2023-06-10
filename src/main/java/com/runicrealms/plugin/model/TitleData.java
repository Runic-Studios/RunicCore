package com.runicrealms.plugin.model;

import java.util.HashSet;
import java.util.Set;

public class TitleData {
    private Set<String> unlockedTitles = new HashSet<>();
    private String prefix = "";
    private String suffix = "";

    /**
     * Constructor for Spring and new players
     */
    @SuppressWarnings("unused")
    public TitleData() {
        // Default constructor for Spring
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified prefix
     *
     * @param prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the current in-memory prefix of the player to the specified suffix
     *
     * @param suffix to set
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Set<String> getUnlockedTitles() {
        return unlockedTitles;
    }

    public void setUnlockedTitles(Set<String> unlockedTitles) {
        this.unlockedTitles = unlockedTitles;
    }

    private void unlockTitle(String title) {
        this.unlockedTitles.add(title);
    }

}
