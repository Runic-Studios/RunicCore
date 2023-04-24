package com.runicrealms.plugin.spellapi.skilltrees;

import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class PerkSpell extends Perk {
    private String spellName;

    public PerkSpell(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints, String spellName) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.spellName = spellName;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }

    @Override
    public Document writeToDocument(Perk perk, Document document) {
        try {
            document = super.writeToDocument(perk, document);
            document.put("type", "spell");
            document.put("spellName", this.spellName);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().log(Level.SEVERE, "Something went wrong writing a perk spell to mongo!");
        }
        return document;
    }
}
