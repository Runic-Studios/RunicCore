package com.runicrealms.plugin.spellapi.spelltypes;

public class Shield {

    private final double amount;
    private final long startTime;

    /**
     * pair of double (the shield amount) and long (the time the shield was applied)
     *
     * @param amount    of the shield
     * @param startTime when the shield was last applied
     */
    public Shield(double amount, long startTime) {
        this.amount = amount;
        this.startTime = startTime;
    }

    public double getAmount() {
        return amount;
    }

    public long getStartTime() {
        return startTime;
    }
}
