package com.runicrealms.plugin.spellapi.skilltrees;

import org.bson.Document;

public abstract class Perk {
    private Integer perkID;
    private int cost;
    private int currentlyAllocatedPoints;
    private int maxAllocatedPoints;

    /**
     * A perk is a super class for PerkStat or PerkSpell, which builds skill trees
     *
     * @param perkID                   a unique integer for database storage
     * @param cost                     how many skill points spent per allocation, generally 1
     * @param currentlyAllocatedPoints how many points the player has currently spent on the perk
     * @param maxAllocatedPoints       how many points can be spent on the perk
     */
    public Perk(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints) {
        this.perkID = perkID;
        this.cost = cost;
        this.currentlyAllocatedPoints = currentlyAllocatedPoints;
        this.maxAllocatedPoints = maxAllocatedPoints;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
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

    public Integer getPerkID() {
        return perkID;
    }

    public void setPerkID(Integer perkID) {
        this.perkID = perkID;
    }

    /**
     * Writes a perk to mongo. Override in child methods
     *
     * @param source   the perk to write
     * @param document the document to modify
     * @return the modified document
     */
    public Document writeToDocument(Perk source, Document document) {
        document.put("cost", source.getCost());
        document.put("currentPoints", source.getCurrentlyAllocatedPoints());
        document.put("maxPoints", source.getMaxAllocatedPoints());
        document.put("perkId", source.getPerkID());
        return document;
    }

}
