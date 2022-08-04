package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.runicitems.Stat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class StatManager implements Listener {

    private final HashMap<UUID, StatContainer> playerStatMap;

    public StatManager() {
        playerStatMap = new HashMap<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    // Fire as HIGHEST so that runic items loads cached stats first for base stats tree grab, and SkillTreeManager loads skill trees
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(CharacterSelectEvent e) {
        StatContainer statContainer = new StatContainer(e.getPlayer());
        UUID uuid = e.getPlayer().getUniqueId();
        playerStatMap.put(e.getPlayer().getUniqueId(), statContainer);
        grabBaseStatsFromTree(uuid, 1);
        grabBaseStatsFromTree(uuid, 2);
        grabBaseStatsFromTree(uuid, 3);
    }

    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        playerStatMap.remove(e.getPlayer().getUniqueId());
    }

    /**
     * Loads the base stats across all subclass trees into memory.
     *
     * @param uuid         of player to load stats for
     * @param treePosition which subtree are we loading? (1,2,3)
     */
    private void grabBaseStatsFromTree(UUID uuid, int treePosition) {
        if (RunicCoreAPI.getSkillTree(uuid, treePosition) == null) return;
        for (Perk perk : RunicCoreAPI.getSkillTree(uuid, treePosition).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkBaseStat)) continue;
            PerkBaseStat perkBaseStat = (PerkBaseStat) perk;
            Stat stat = perkBaseStat.getStat();
            int amount = perkBaseStat.getBonusAmount() * perkBaseStat.getCurrentlyAllocatedPoints();
            playerStatMap.get(uuid).increaseStat(stat, amount);
        }
    }

    public StatContainer getPlayerStatContainer(UUID uuid) {
        return playerStatMap.get(uuid);
    }
}
