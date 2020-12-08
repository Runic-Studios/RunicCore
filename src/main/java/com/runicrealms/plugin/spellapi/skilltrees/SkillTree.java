package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.spellapi.skilltrees.util.MageTreeUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {

    private Player player;
    private final List<Perk> perks;
    private static final String PATH_LOCATION = "skillTree";

    public SkillTree() {
        perks = new ArrayList<>();
    }

    public SkillTree(Player player) {
        this.player = player;
        perks = MageTreeUtil.pyromancerPerkList(); // todo: determine 3 sub-trees to show based on class, then from there
        RunicCore.getSkillTreeManager().getSkillTrees().add(this);
        // get sub-class, load default
        // get allocatin data from db, populate
        updateValuesFromDB();
    }

    public Player getPlayer() {
        return player;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    // todo: grab data from DB once
    public void updateValuesFromDB() {

    }

    /**
     * Saves all perks which have at least 1 point allocated to DB!
     * @param mongoData called from PlayerCache
     * @param slot of selected class
     */
    public void save(PlayerMongoData mongoData, int slot) {
        MongoDataSection treeDataSection = mongoData.getCharacter(slot);
        for (Perk perk : perks) {
            if (perk.getCurrentlyAllocatedPoints() == 0) continue;
            treeDataSection.set(PATH_LOCATION + "." + perk.getPerkID(), perk.getCurrentlyAllocatedPoints());
        }
        treeDataSection.save();
    }
}
