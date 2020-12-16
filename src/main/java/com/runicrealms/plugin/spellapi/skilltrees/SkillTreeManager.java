package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/*
 * Caches three skill trees per-player, one for each sub-class.
 */
public class SkillTreeManager implements Listener {

    private final HashSet<SkillTree> skillTreeSetOne; // first sub-class
    private final HashSet<SkillTree> skillTreeSetTwo; // second sub-class
    private final HashSet<SkillTree> skillTreeSetThree; // third sub-class

    public SkillTreeManager() {
        skillTreeSetOne = new HashSet<>();
        skillTreeSetTwo = new HashSet<>();
        skillTreeSetThree = new HashSet<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /*
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
    }

    /*
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
