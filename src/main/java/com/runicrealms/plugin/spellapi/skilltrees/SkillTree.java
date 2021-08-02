package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.classes.utilities.SubClassUtil;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import com.runicrealms.plugin.spellapi.skilltrees.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class SkillTree {

    private final int position;
    private final SubClassEnum subClassEnum;
    private final Player player;
    private final List<Perk> perks;
    public static final String PATH_LOCATION = "skillTree";
    public static final String POINTS_LOCATION = "spentPoints";
    public static final String SPELLS_LOCATION = "spells";

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

    /**
     * Cacluates the available skill points of the player.
     * First point is given at level 10, so first 9 levels are ignored.
     *
     * @return available skill points to spend
     */
    public static int getAvailablePoints(Player player) {
        int spentPoints = RunicCoreAPI.getSpentPoints(player);
        return Math.max(0, player.getLevel() - 9 - spentPoints);
    }

    /**
     * Attempts to purchase perk selected from inv click event.
     * Will fail for a variety of reasons, or succeeds, updates currently allocated points, and
     *
     * @param previous the previous perk in the perk array (to ensure perks purchased in-sequence)
     * @param perk     the perk attempting to be purchased
     */
    public void attemptToPurchasePerk(Perk previous, Perk perk) {
        int getAvailablePoints = getAvailablePoints(player);
        if (perk.getCurrentlyAllocatedPoints() >= perk.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
            return;
        }
        if (previous != null && previous.getCurrentlyAllocatedPoints() < previous.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You must purchase all previous perks first!");
            return;
        }
        if (getAvailablePoints <= 0 || getAvailablePoints < perk.getCost()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough skill points to purchase this!");
            return;
        }
        RunicCore.getSkillTreeManager().getSpentPoints().put(player.getUniqueId(),
                RunicCoreAPI.getSpentPoints(player) + perk.getCost()); // spend a point
        perk.setCurrentlyAllocatedPoints(perk.getCurrentlyAllocatedPoints() + 1);
        if (perk instanceof PerkSpell && (RunicCoreAPI.getSpell((((PerkSpell) perk).getSpellName())).isPassive()))
            applyPassives(RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player));
        else if (perk instanceof PerkBaseStat)
            RunicCore.getStatManager().getPlayerStatContainer(player.getUniqueId()).increaseStat(((PerkBaseStat) perk).getStat(), ((PerkBaseStat) perk).getBonusAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        player.sendMessage(ChatColor.GREEN + "You purchased a new perk!");
    }

    /**
     * Populates currently allocated points from DB once on skill tree load
     */
    private void updateValuesFromDB() {
        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection character = mongoData.getCharacter(RunicCoreAPI.getPlayerCache(player).getCharacterSlot());
        if (!character.has(PATH_LOCATION + "." + position)) return;  // DB not populated
        MongoDataSection perkSection = character.getSection(PATH_LOCATION + "." + position);
        for (String key : perkSection.getKeys()) {
            if (getPerk(Integer.parseInt(key)) == null) continue;
            getPerk(Integer.parseInt(key)).setCurrentlyAllocatedPoints(perkSection.get(key, Integer.class));
        }
    }

    /**
     * Loops through currently purchased perks to store passives in memory
     *
     * @param playerSpellWrapper the wrapper object to store the passives in
     */
    public void applyPassives(PlayerSpellWrapper playerSpellWrapper) {
        for (Perk perk : perks) {
            if (perk instanceof PerkBaseStat) continue;
            if (RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName()) == null) continue;
            if (!RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName()).isPassive()) continue;
            if (perk.getCurrentlyAllocatedPoints() >= perk.getCost())
                playerSpellWrapper.getPassives().add(((PerkSpell) perk).getSpellName());
        }
    }

    /**
     * Saves all perks which have at least 1 point allocated to DB!
     *
     * @param character The character section of a DB object
     */
    public void save(PlayerMongoDataSection character) {
        for (Perk perk : perks) {
            if (perk.getCurrentlyAllocatedPoints() == 0) continue;
            character.set(PATH_LOCATION + "." + position + "." + perk.getPerkID(), perk.getCurrentlyAllocatedPoints());
        }
    }

    /**
     * Resets the skill trees for given player. ALL THREE skill trees will be wiped from memory / DB,
     * and spent points will be reset to 0 in DB and memory.
     *
     * @param player to reset tree for
     */
    public static void resetTree(Player player) {
        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection character = mongoData.getCharacter(RunicCoreAPI.getPlayerCache(player).getCharacterSlot());
        character.remove(PATH_LOCATION); // removes ALL THREE SkillTree data sections AND spent points
        character.save();
        mongoData.save();
        RunicCore.getSkillTreeManager().getSkillTreeSetOne().remove(RunicCoreAPI.getSkillTree(player, 1));
        RunicCore.getSkillTreeManager().getSkillTreeSetTwo().remove(RunicCoreAPI.getSkillTree(player, 2));
        RunicCore.getSkillTreeManager().getSkillTreeSetThree().remove(RunicCoreAPI.getSkillTree(player, 3));
        RunicCore.getSkillTreeManager().getSpentPoints().put(player.getUniqueId(), 0);
        RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).clearSpells();
        RunicCore.getStatManager().getPlayerStatContainer(player.getUniqueId()).resetValues();
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your skill trees have been reset!");
    }

    /**
     * Returns the appropriate default perk list for the given sub-class, to be populated
     * later by persistent data from the DB.
     *
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
     * Returns specified perk by ID.
     *
     * @param perkID ID of perk
     * @return the Perk object
     */
    private Perk getPerk(int perkID) {
        for (Perk perk : perks) {
            if (perk.getPerkID() == perkID)
                return perk;
        }
        return null;
    }
}
