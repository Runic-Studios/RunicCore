package com.runicrealms.plugin.model;

/**
 *
 */
public class OutlawInfo {
    private final boolean outlawEnabled;
    private final int outlawRating;

    /**
     * @param outlawEnabled
     * @param outlawRating
     */
    public OutlawInfo(boolean outlawEnabled, int outlawRating) {
        this.outlawEnabled = outlawEnabled;
        this.outlawRating = outlawRating;
    }

    public boolean isOutlawEnabled() {
        return outlawEnabled;
    }

    public int getOutlawRating() {
        return outlawRating;
    }
}
