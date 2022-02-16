package com.runicrealms.plugin.utilities;

/**
 * Adding this manually because this language should just have it.
 *
 * @param <X>
 * @param <Y>
 * @author Skyfallin
 */
public class Tuple<X, Y> {
    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}
