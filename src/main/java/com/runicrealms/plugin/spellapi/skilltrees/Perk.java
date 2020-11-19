package com.runicrealms.plugin.spellapi.skilltrees;

public abstract class Perk {

    private int cost;
    private int minPointsReq;
    private int maxAllocatedPoints;

    public Perk(int cost, int minPointsReq, int maxAllocatedPoints) {
        this.cost = cost;
        this.minPointsReq = minPointsReq;
        this.maxAllocatedPoints = maxAllocatedPoints;
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

    public int getMaxAllocatedPoints() {
        return maxAllocatedPoints;
    }

    public void setMaxAllocatedPoints(int maxAllocatedPoints) {
        this.maxAllocatedPoints = maxAllocatedPoints;
    }
}
