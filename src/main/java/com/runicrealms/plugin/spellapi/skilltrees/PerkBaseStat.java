package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.runicitems.Stat;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class PerkBaseStat extends Perk {
    private static final int DEFAULT_BONUS = 2;
    private final int bonusAmount;
    private final Stat stat;

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints,
                        Stat stat, int bonusAmount) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.stat = stat;
        this.bonusAmount = bonusAmount;
    }

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints, Stat stat) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.stat = stat;
        this.bonusAmount = DEFAULT_BONUS;
    }

    public int getBonusAmount() {
        return bonusAmount;
    }

    public Stat getStat() {
        return stat;
    }

    @Override
    public Document writeToDocument(Perk perk, Document document) {
        try {
            document = super.writeToDocument(perk, document);
            document.put("type", "stat");
            document.put("bonus", this.getBonusAmount());
            document.put("stat", this.getStat().getIdentifier());
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Something went wrong writing a perk base stat to mongo!");
        }
        return document;
    }
}
