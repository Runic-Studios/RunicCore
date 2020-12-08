package com.runicrealms.plugin.spellapi.skilltrees;

public abstract class Perk {

    private final int perkID;
    private int cost;
    private int minPointsReq;
    private int currentlyAllocatedPoints;
    private int maxAllocatedPoints;

    /**
     * A perk is a super class for PerkStat or PerkSpell, which builds skill trees
     * @param perkID a unique integer for database storage
     * @param cost how many skill points spent per allocation, generally 1
     * @param minPointsReq minimum points required IN CURRENT SUB-TREE to purchase perk (prevent buying ultimate spells from other classes)
     * @param currentlyAllocatedPoints how many points the player has currently spent on the perk
     * @param maxAllocatedPoints how many points can be spent on the perk
     */
    public Perk(int perkID, int cost, int minPointsReq, int currentlyAllocatedPoints, int maxAllocatedPoints) {
        this.perkID = perkID;
        this.cost = cost;
        this.minPointsReq = minPointsReq;
        this.currentlyAllocatedPoints = currentlyAllocatedPoints;
        this.maxAllocatedPoints = maxAllocatedPoints;
    }

    public int getPerkID() {
        return perkID;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getMinPointsReq() {
        return minPointsReq;
    }

    public void setMinPointsReq(int minPointsReq) {
        this.minPointsReq = minPointsReq;
    }

    public int getCurrentlyAllocatedPoints() {
        return currentlyAllocatedPoints;
    }

    public void setCurrentlyAllocatedPoints(int currentlyAllocatedPoints) {
        this.currentlyAllocatedPoints = currentlyAllocatedPoints;
    }

    public int getMaxAllocatedPoints() {
        return maxAllocatedPoints;
    }

    public void setMaxAllocatedPoints(int maxAllocatedPoints) {
        this.maxAllocatedPoints = maxAllocatedPoints;
    }
}
