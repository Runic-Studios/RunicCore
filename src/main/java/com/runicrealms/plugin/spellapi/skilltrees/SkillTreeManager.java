package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/**
 * Caches three skill trees per-player, one for each sub-class.
 */
public class SkillTreeManager implements Listener {

    private final HashSet<SkillTree> skillTreeOne; // first sub-class
    private final HashSet<SkillTree> skillTreeTwo; // second sub-class
    private final HashSet<SkillTree> skillTreeThree; // third sub-class

    public SkillTreeManager() {
        skillTreeOne = new HashSet<>();
        skillTreeTwo = new HashSet<>();
        skillTreeThree = new HashSet<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /*

     */
    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                skillTreeOne.remove(searchSkillTree(e.getPlayer(), 1));
                skillTreeTwo.remove(searchSkillTree(e.getPlayer(), 2));
                skillTreeThree.remove(searchSkillTree(e.getPlayer(), 3));
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    /**
     *
     * @param position
     * @return
     */
    public HashSet<SkillTree> getSkillTree(int position) {
        if (position == 1)
            return skillTreeOne;
        else if (position == 2)
            return skillTreeTwo;
        else
            return skillTreeThree;
    }

    public HashSet<SkillTree> getSkillTreeOne() {
        return skillTreeOne;
    }

    public HashSet<SkillTree> getSkillTreeTwo() {
        return skillTreeTwo;
    }

    public HashSet<SkillTree> getSkillTreeThree() {
        return skillTreeThree;
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
