package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/*
 * Caches three skill trees per-player, one for each sub-class.
 */
public class SkillTreeManager implements Listener {

    private final HashMap<UUID, Integer> spentPoints; // allocated points across all skill trees
    private final HashSet<SkillTree> skillTreeSetOne; // first sub-class
    private final HashSet<SkillTree> skillTreeSetTwo; // second sub-class
    private final HashSet<SkillTree> skillTreeSetThree; // third sub-class

    public SkillTreeManager() {
        spentPoints = new HashMap<>();
        skillTreeSetOne = new HashSet<>();
        skillTreeSetTwo = new HashSet<>();
        skillTreeSetThree = new HashSet<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Saves player skill tree info whenever the player cache is saved.
     */
    @EventHandler
    public void onCacheSave(CacheSaveEvent e) {
        if (RunicCoreAPI.getSkillTree(e.getPlayer(), 1) != null)
            RunicCoreAPI.getSkillTree(e.getPlayer(), 1).save(e.getMongoDataSection());
        if (RunicCoreAPI.getSkillTree(e.getPlayer(), 2) != null)
            RunicCoreAPI.getSkillTree(e.getPlayer(), 2).save(e.getMongoDataSection());
        if (RunicCoreAPI.getSkillTree(e.getPlayer(), 3) != null)
            RunicCoreAPI.getSkillTree(e.getPlayer(), 3).save(e.getMongoDataSection());
        if (spentPoints.get(e.getPlayer().getUniqueId()) != 0)
            saveSpentPoints(e.getPlayer(), e.getMongoDataSection());
    }

    /**
     * Setup in-memory map of "spent points," tracking how many skill points a player has already allocated
     * from the total available at-level.
     */
    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        Player player = e.getPlayer();
        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection character = mongoData.getCharacter(RunicCoreAPI.getPlayerCache(player).getCharacterSlot());
        int points = 0;
        if (character.has(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION))
            points = Integer.parseInt(character.get(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION).toString());
        if (points < 0) // insurance
            points = 0;
        if (points > PlayerLevelUtil.getMaxLevel() - 9)
            points = PlayerLevelUtil.getMaxLevel() - 9;
        spentPoints.put(player.getUniqueId(), points);
    }

    /**
     * Removes player skill trees from memory on logout.
     */
    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                skillTreeSetOne.remove(searchSkillTree(e.getPlayer(), 1));
                skillTreeSetTwo.remove(searchSkillTree(e.getPlayer(), 2));
                skillTreeSetThree.remove(searchSkillTree(e.getPlayer(), 3));
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    /**
     * Saves the total spent points to DB
     * @param character mongo data section from save event
     */
    private void saveSpentPoints(Player player, PlayerMongoDataSection character) {
        character.set(SkillTree.PATH_LOCATION + "." + SkillTree.POINTS_LOCATION, RunicCoreAPI.getSpentPoints(player));
    }

    /**
     * Grabs the associated in-memory skill tree cache
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

    /**
     * Checks cached skill trees for given player.
     * @param player to search for in cache
     * @return player's cached SkillTree
     */
    public SkillTree searchSkillTree(Player player, int position) {
        HashSet<SkillTree> toSearch = getSkillTree(position);
        for (SkillTree skillTree : toSearch) {
            if (skillTree.getPlayer().equals(player))
                return skillTree;
        }
        return null;
    }
}
