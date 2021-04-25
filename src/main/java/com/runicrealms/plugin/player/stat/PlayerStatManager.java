package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStatManager implements Listener {

    private final HashMap<UUID, StatContainer> playerStatMap;

    public PlayerStatManager() {
        playerStatMap = new HashMap<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        StatContainer statContainer = new StatContainer(e.getPlayer());
        playerStatMap.put(e.getPlayer().getUniqueId(), statContainer);
        grabBaseStatsFromTree(e.getPlayer(), 1);
        grabBaseStatsFromTree(e.getPlayer(), 2);
        grabBaseStatsFromTree(e.getPlayer(), 3);
    }

    // todo: then we update gear scanner

    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        playerStatMap.remove(e.getPlayer().getUniqueId());
    }

    /**
     * Loads the base stats across all sub-class trees into memory.
     * @param player player to load stats for
     * @param treePosition which sub-tree are we loading? (1,2,3)
     */
    private void grabBaseStatsFromTree(Player player, int treePosition) {
        if (RunicCoreAPI.getSkillTree(player, treePosition) == null) return;
        for (Perk perk : RunicCoreAPI.getSkillTree(player, treePosition).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkBaseStat)) continue;
            PerkBaseStat perkBaseStat = (PerkBaseStat) perk;
            BaseStatEnum baseStatEnum = perkBaseStat.getBaseStatEnum();
            int amount = perkBaseStat.getBonusAmount() * perkBaseStat.getCurrentlyAllocatedPoints();
            playerStatMap.get(player.getUniqueId()).increaseStat(baseStatEnum, amount);
        }
    }

    public StatContainer getPlayerStatContainer(UUID uuid) {
        return playerStatMap.get(uuid);
    }
}
