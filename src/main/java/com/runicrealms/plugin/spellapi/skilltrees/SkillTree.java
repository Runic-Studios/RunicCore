package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.classes.utilities.SubClassUtil;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.spellapi.skilltrees.util.*;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillTree {

    private final int position;
    private final SubClassEnum subClassEnum;
    private final Player player;
    private final List<Perk> perks;
    private static final String PATH_LOCATION = "skillTree";

    public SkillTree(Player player, int position) {
        this.position = position;
        subClassEnum = SubClassUtil.determineSubClass(player, position);
        this.player = player;
        // get sub-class, load default
        perks = getSkillTreeBySubClass(subClassEnum);
        RunicCore.getSkillTreeManager().getSkillTree(position).add(this);
        // get allocatin data from db, populate
        updateValuesFromDB();
    }

    // todo: grab data from DB once
    public void updateValuesFromDB() {

    }

    /**
     * Saves all perks which have at least 1 point allocated to DB!
     * @param character The character section of a DB object
     */
    public void save(PlayerMongoDataSection character) {
        for (Perk perk : perks) {
            if (perk.getCurrentlyAllocatedPoints() == 0) continue;
            character.set(PATH_LOCATION + "." + position + "." + perk.getPerkID(), perk.getCurrentlyAllocatedPoints());
        }
    }

    /**
     * Removes all saved perks for given player, for use in point resets
     * @param mongoData called from PlayerCache
     * @param slot of selected class
     */
    public void resetTree(PlayerMongoData mongoData, int slot) {
        MongoDataSection treeDataSection = mongoData.getCharacter(slot);
        for (Perk perk : perks) {
            if (perk.getCurrentlyAllocatedPoints() == 0) continue;
            perk.setCurrentlyAllocatedPoints(0);
            treeDataSection.remove(PATH_LOCATION); // removes ALL THREE SkillTree data sections
        }
        treeDataSection.save();
        mongoData.save();
    }

    /**
     * Returns the appropriate default perk list for the given sub-class, to be populated
     * later by persistent data from the DB.
     * @param subClassEnum the sub-class of the player (try SubClassUtil)
     * @return A default list of perks (no purchased perks)
     */
    private List<Perk> getSkillTreeBySubClass(SubClassEnum subClassEnum) throws NullPointerException {
        switch (subClassEnum) {
            case MARKSMAN:
                return ArcherTreeUtil.marksmanPerkList();
            case SCOUT:
                return ArcherTreeUtil.scoutPerkList();
            case WARDEN:
                return ArcherTreeUtil.wardenPerkList();
            case BARD:
                return ClericTreeUtil.bardPerkList();
            case EXEMPLAR:
                return ClericTreeUtil.exemplarList();
            case PRIEST:
                return ClericTreeUtil.priestList();
            case CRYOMANCER:
                return MageTreeUtil.cryomancerPerkList();
            case PYROMANCER:
                return MageTreeUtil.pyromancerPerkList();
            case WARLOCK:
                return MageTreeUtil.warlockPerkList();
            case ASSASSIN:
                return RogueTreeUtil.assassinPerkList();
            case DUELIST:
                return RogueTreeUtil.duelistPerkList();
            case SWINDLER:
                return RogueTreeUtil.swindlerPerkList();
            case BERSERKER:
                return WarriorTreeUtil.berserkerPerkList();
            case GUARDIAN:
                return WarriorTreeUtil.guardianPerkList();
            case INQUISITOR:
                return WarriorTreeUtil.inquisitorPerkList();
        }
        return null;
    }

    public int getPosition() {
        return position;
    }

    public SubClassEnum getSubClassEnum() {
        return subClassEnum;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    /**
     *
     * @param perk
     */
    public void attemptToPurchasePerk(Perk perk) {
        perk.setCurrentlyAllocatedPoints(perk.getCurrentlyAllocatedPoints() + 1);
    }
}
