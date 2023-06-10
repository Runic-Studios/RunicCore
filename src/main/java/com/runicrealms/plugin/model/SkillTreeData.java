package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.SubClass;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.util.ArcherTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.ClericTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.MageTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.RogueTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.WarriorTreeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SkillTreeData {
    public static final int FIRST_POINT_LEVEL = 10;
    private SkillTreePosition position;
    private List<Perk> perks = new ArrayList<>();

    @SuppressWarnings("unused")
    public SkillTreeData() {
        // Default constructor for Spring
    }

    /**
     * Build default skill tree data (if there is no persistent data)
     *
     * @param position of the skill tree (1-3)
     */
    public SkillTreeData(SkillTreePosition position, String playerClass) {
        this.position = position;
        CharacterClass characterClass = CharacterClass.getFromName(playerClass);
        SubClass subClass = SubClass.determineSubClass(characterClass, position);
        this.perks = getSkillTreeBySubClass(subClass); // load default perks
    }

    /**
     * Calculates the available skill points of the player.
     * First point is given at level 10, so first 9 levels are ignored.
     * For level 10 onward, we award 1 point per level. Then,
     * subtract 1 point for each purchased perk across all skill trees
     *
     * @param uuid of the player
     * @param slot of the character
     * @return available skill points to spend
     */
    public static int getAvailablePoints(UUID uuid, int slot) {
        Player player = Bukkit.getPlayer(uuid);
        int spentPoints = RunicCore.getSkillTreeAPI().getSpentPoints(uuid, slot);
        if (player != null)
            return Math.max(0, player.getLevel() - (FIRST_POINT_LEVEL - 1) - spentPoints);
        else
            return 0;
    }

    /**
     * Resets the skill trees for given player. ALL THREE skill trees will be wiped from memory / DB,
     * and spent points will be reset to 0 in DB and memory.
     *
     * @param player to reset tree for
     */
    public static void resetSkillTrees(Player player) {
        UUID uuid = player.getUniqueId();
        /*
        Wipe the memoized perk data
         */
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
        Map<SkillTreePosition, SkillTreeData> skillTreeDataMap = RunicCore.getSkillTreeAPI().getSkillTreeDataMap(uuid, slot);
        SkillTreeData first = skillTreeDataMap.get(SkillTreePosition.FIRST);
        SkillTreeData second = skillTreeDataMap.get(SkillTreePosition.SECOND);
        SkillTreeData third = skillTreeDataMap.get(SkillTreePosition.THIRD);
        first.setPerks(first.getSkillTreeBySubClass(first.getSubClass(uuid)));
        second.setPerks(second.getSkillTreeBySubClass(second.getSubClass(uuid)));
        third.setPerks(third.getSkillTreeBySubClass(third.getSubClass(uuid)));
        // --------------------------------------------
        SpellData spellData = RunicCore.getSkillTreeAPI().getPlayerSpellData(uuid, slot);
        spellData.resetSpells(RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid)); // reset assigned spells in-memory
        RunicCore.getSkillTreeAPI().getPassives(uuid).clear(); // reset passives
        RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId()).resetValues(); // reset stat values
        RunicCore.getCoreWriteOperation().updateCorePlayerData
                (
                        uuid,
                        slot,
                        "spellDataMap",
                        spellData,
                        () -> player.sendMessage(ChatColor.LIGHT_PURPLE + "Your skill trees have been reset!")
                );
    }

    public long getTotalPoints() {
        AtomicInteger result = new AtomicInteger();
        Stream<Perk> boughtPerks = this.perks.stream().filter(perk -> perk.getCurrentlyAllocatedPoints() > 0);
        boughtPerks.forEach(perk -> result.addAndGet(perk.getCurrentlyAllocatedPoints()));
        return result.get();
    }

    /**
     * Loops through currently purchased perks to store passives in memory
     */
    public void addPassivesToMap(UUID uuid) {
        if (RunicCore.getSkillTreeAPI().getPassives(uuid) == null)
            return; // player is offline or removed from memory
        for (Perk perk : perks) {
            if (perk instanceof PerkBaseStat) continue;
            if (RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()) == null)
                continue;
            if (!RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()).isPassive())
                continue;
            if (perk.getCurrentlyAllocatedPoints() >= perk.getCost())
                RunicCore.getSkillTreeAPI().getPassives(uuid).add(((PerkSpell) perk).getSpellName());
        }
    }

    /**
     * Attempts to purchase perk selected from inv click event.
     * Will fail for a variety of reasons, or succeeds, updates currently allocated points, and
     *
     * @param player   purchasing the perk
     * @param slot     of the character
     * @param previous the previous perk in the perk array (to ensure perks purchased in-sequence)
     * @param perk     the perk attempting to be purchased
     */
    public boolean attemptToPurchasePerk(Player player, int slot, Perk previous, Perk perk) {
        int getAvailablePoints = getAvailablePoints(player.getUniqueId(), slot);
        if (perk.getCurrentlyAllocatedPoints() >= perk.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
            return false;
        }
        if (previous != null && previous.getCurrentlyAllocatedPoints() < previous.getMaxAllocatedPoints()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You must purchase all previous perks first!");
            return false;
        }
        if (getAvailablePoints <= 0 || getAvailablePoints < perk.getCost()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough skill points to purchase this!");
            return false;
        }
        // Purchase perk!
        perk.setCurrentlyAllocatedPoints(perk.getCurrentlyAllocatedPoints() + 1);
        if (perk instanceof PerkSpell && (RunicCore.getSpellAPI().getSpell((((PerkSpell) perk).getSpellName())).isPassive()))
            addPassivesToMap(player.getUniqueId());
            // Increase stat SYNC
        else if (perk instanceof PerkBaseStat) {
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> RunicCore.getStatAPI().getPlayerStatContainer(player.getUniqueId())
                            .increaseStat(((PerkBaseStat) perk).getStat(), ((PerkBaseStat) perk).getBonusAmount()));
        }


        UUID uuid = player.getUniqueId();
        // Retrieve the updated SkillTreeData from our in-memory storage, ensure it is up-to-date for this tree
        Map<SkillTreePosition, SkillTreeData> updatedSkillTreeData = RunicCore.getSkillTreeAPI().getSkillTreeDataMap(uuid, slot);
        updatedSkillTreeData.put(this.position, this);
        // Update in mongo
        RunicCore.getCoreWriteOperation().updateCorePlayerData
                (
                        uuid,
                        slot,
                        "skillTreeDataMap",
                        updatedSkillTreeData,
                        () -> {
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                            player.sendMessage(ChatColor.GREEN + "You purchased a new perk!");
                        }
                );
        return true;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public void setPerks(List<Perk> perks) {
        this.perks = perks;
    }

    public SkillTreePosition getPosition() {
        return position;
    }

    public void setPosition(SkillTreePosition position) {
        this.position = position;
    }

    /**
     * Returns the appropriate default perk list for the given subclass, to be populated
     * later by persistent data from the DB.
     *
     * @param subClass the subclass of the player (try SubClassUtil)
     * @return A default list of perks (no purchased perks)
     */
    private List<Perk> getSkillTreeBySubClass(SubClass subClass) throws NullPointerException {
        return switch (subClass) {
            case MARKSMAN -> ArcherTreeUtil.marksmanPerkList();
            case STORMSHOT -> ArcherTreeUtil.stormshotPerkList();
            case WARDEN -> ArcherTreeUtil.wardenPerkList();
            case BARD -> ClericTreeUtil.bardPerkList();
            case LIGHTBRINGER -> ClericTreeUtil.lightbringerPerkList();
            case STARWEAVER -> ClericTreeUtil.starweaverPerkList();
            case CRYOMANCER -> MageTreeUtil.cryomancerPerkList();
            case PYROMANCER -> MageTreeUtil.pyromancerPerkList();
            case SPELLSWORD -> MageTreeUtil.spellswordPerkList();
            case CORSAIR -> RogueTreeUtil.corsairPerkList();
            case WITCH_HUNTER -> RogueTreeUtil.witchHunterPerkList();
            case NIGHTCRAWLER -> RogueTreeUtil.nightcrawlerPerkList();
            case BERSERKER -> WarriorTreeUtil.berserkerPerkList();
            case DREADLORD -> WarriorTreeUtil.dreadlordPerkList();
            case PALADIN -> WarriorTreeUtil.paladinPerkList();
        };
    }

    public SubClass getSubClass(UUID uuid) {
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(uuid);
        CharacterClass characterClass = CharacterClass.getFromName(className);
        return SubClass.determineSubClass(characterClass, position);
    }

}
