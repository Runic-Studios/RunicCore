package com.runicrealms.plugin.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedRandomBag<T> {

    private final List<Entry> entries = new ArrayList<>();
    private final Random rand = new Random();
    private double accumulatedWeight;

    public WeightedRandomBag() {

    }

    public WeightedRandomBag(WeightedRandomBag<T> weightedRandomBag) {
        for (Entry entry : weightedRandomBag.entries) {
            addEntry(entry.object, entry.weight);
        }
    }

    public void addEntry(T object, double weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.weight = weight;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }

    public T getRandom() {
        double r = rand.nextDouble() * accumulatedWeight;

        for (Entry entry : entries) {
            if (entry.accumulatedWeight >= r) {
                return entry.object;
            }
        }
        return null; // should only happen when there are no entries
    }

    private class Entry {
        double weight;
        double accumulatedWeight;
        T object;
    }
}