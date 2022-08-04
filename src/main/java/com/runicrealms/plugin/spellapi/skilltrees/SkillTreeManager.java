package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Caches three skill trees per-player, one for each sub-class.
 */
public class SkillTreeManager implements Listener {

    private final HashMap<UUID, Integer> spentPoints; // allocated points across all skill trees
    private final HashSet<SkillTree> skillTreeSetOne; // first sub-class
    private final HashSet<SkillTree> skillTreeSetTwo; // second sub-class
    private final HashSet<SkillTree> skillTreeSetThree; // third sub-class
    private final HashSet<PlayerSpellWrapper> playerSpellWrappers;

    public SkillTreeManager() {
        spentPoints = new HashMap<>();
        skillTreeSetOne = new HashSet<>();
        skillTreeSetTwo = new HashSet<>();
        skillTreeSetThree = new HashSet<>();
        this.playerSpellWrappers = new HashSet<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Saves player skill tree info whenever the player cache is saved.
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent e) {
        UUID uuid = e.getUuid();
        if (RunicCoreAPI.getSkillTree(uuid, 1) != null)
            RunicCoreAPI.getSkillTree(uuid, 1).save(e.getMongoDataSection());
        if (RunicCoreAPI.getSkillTree(uuid, 2) != null)
            RunicCoreAPI.getSkillTree(uuid, 2).save(e.getMongoDataSection());
        if (RunicCoreAPI.getSkillTree(uuid, 3) != null)
            RunicCoreAPI.getSkillTree(uuid, 3).save(e.getMongoDataSection());
        if (spentPoints.get(uuid) != 0)
            saveSpentPoints(uuid, e.getMongoDataSection());
        if (getPlayerSpellWrapper(uuid) != null)
            saveSpells(getPlayerSpellWrapper(uuid), e.getMongoDataSection());
    }

    /**
     * Setup in-memory map of all three subclass skill trees and "spent points," tracking how many
     * skill points a player has already allocated from the total available at-level.
     */
    @EventHandler(priority = EventPriority.HIGH) // loads last, but BEFORE StatManager
    public void onLoad(CharacterSelectEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        int slot = e.getCharacterData().getBaseCharacterInfo().getSlot();
        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
        MongoDataSection character = playerMongoData.getCharacter(slot);
        new SkillTree(uuid, 1);
        new SkillTree(uuid, 2);
        new SkillTree(uuid, 3);
        int points = 0;
        if (character.has(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION))
            points = character.get(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION, Integer.class);
        if (points < 0) // insurance
            points = 0;
        if (points > PlayerLevelUtil.getMaxLevel() - (SkillTree.FIRST_POINT_LEVEL - 1))
            points = PlayerLevelUtil.getMaxLevel() - (SkillTree.FIRST_POINT_LEVEL - 1);
        spentPoints.put(player.getUniqueId(), points);
        if (character.has(SkillTree.PATH_LOCATION + "." + SkillTree.SPELLS_LOCATION))
            new PlayerSpellWrapper(uuid,
                    (PlayerMongoDataSection) character.getSection(SkillTree.PATH_LOCATION + "." + SkillTree.SPELLS_LOCATION));
        else
            new PlayerSpellWrapper(uuid, PlayerSpellWrapper.determineDefaultSpell(uuid), "",
                    "", "");
    }

    /**
     * Removes player skill trees from memory on logout.
     */
    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                skillTreeSetOne.remove(searchSkillTree(uuid, 1));
                skillTreeSetTwo.remove(searchSkillTree(uuid, 2));
                skillTreeSetThree.remove(searchSkillTree(uuid, 3));
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    /**
     * Saves the total spent points to DB
     *
     * @param uuid      of the player
     * @param character mongo data section from save event
     */
    private void saveSpentPoints(UUID uuid, PlayerMongoDataSection character) {
        character.set(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION, RunicCoreAPI.getSpentPoints(uuid));
    }

    /**
     * Saves the alt-specific spell setup for given player
     *
     * @param playerSpellWrapper wrapper of in-memory spells
     * @param character          character section of mongo
     */
    private void saveSpells(PlayerSpellWrapper playerSpellWrapper, PlayerMongoDataSection character) {
        PlayerMongoDataSection spells = (PlayerMongoDataSection) character.getSection(SkillTree.PATH_LOCATION + "." + SkillTree.SPELLS_LOCATION);
        spells.set(PlayerSpellWrapper.PATH_1, playerSpellWrapper.getSpellHotbarOne());
        spells.set(PlayerSpellWrapper.PATH_2, playerSpellWrapper.getSpellLeftClick());
        spells.set(PlayerSpellWrapper.PATH_3, playerSpellWrapper.getSpellRightClick());
        spells.set(PlayerSpellWrapper.PATH_4, playerSpellWrapper.getSpellSwapHands());
    }

    /**
     * Grabs the associated in-memory skill tree cache
     *
     * @param position of the sub-class (1, 2, or 3)
     * @return a HashSet of cached skill trees
     */
    public HashSet<SkillTree> getSkillTree(int position) {
        if (position == 1)
            return skillTreeSetOne;
        else if (position == 2)
            return skillTreeSetTwo;
        else
            return skillTreeSetThree;
    }

    public HashMap<UUID, Integer> getSpentPoints() {
        return spentPoints;
    }

    public HashSet<SkillTree> getSkillTreeSetOne() {
        return skillTreeSetOne;
    }

    public HashSet<SkillTree> getSkillTreeSetTwo() {
        return skillTreeSetTwo;
    }

    public HashSet<SkillTree> getSkillTreeSetThree() {
        return skillTreeSetThree;
    }

    public HashSet<PlayerSpellWrapper> getPlayerSpellWrappers() {
        return playerSpellWrappers;
    }

    /**
     * Gets the spell wrapper for given player
     *
     * @param uuid of player to return wrapper for
     * @return spell wrapper
     */
    public PlayerSpellWrapper getPlayerSpellWrapper(UUID uuid) {
        for (PlayerSpellWrapper playerSpellWrapper : playerSpellWrappers) {
            if (playerSpellWrapper.getUuid().equals(uuid))
                return playerSpellWrapper;
        }
        return null;
    }

    /**
     * Checks cached skill trees for given player.
     *
     * @param uuid of player to search for in cache
     * @return player's cached SkillTree
     */
    public SkillTree searchSkillTree(UUID uuid, int position) {
        HashSet<SkillTree> toSearch = getSkillTree(position);
        for (SkillTree skillTree : toSearch) {
            if (skillTree.getUuid().equals(uuid))
                return skillTree;
        }
        return null;
    }
}
