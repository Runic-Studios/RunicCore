package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class SkillTreeManager implements Listener {

    private final HashSet<SkillTree> skillTrees;

    public SkillTreeManager() {
        skillTrees = new HashSet<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /*

     */
    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                skillTrees.remove(getSkillTree(e.getPlayer()));
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    public HashSet<SkillTree> getSkillTrees() {
        return skillTrees;
    }

    /**
     * Checks cached skill trees for given player.
     * @param player to search for in cache
     * @return player's cached SkillTree
     */
    public SkillTree getSkillTree(Player player) {
        for (SkillTree skillTree : skillTrees) {
            if (skillTree.getPlayer().equals(player))
                return skillTree;
        }
        return null;
    }
}
