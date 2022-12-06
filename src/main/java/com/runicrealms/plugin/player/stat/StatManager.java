package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.UUID;

public class StatManager implements Listener {

    private final HashMap<UUID, StatContainer> playerStatMap;

    public StatManager() {
        playerStatMap = new HashMap<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    public StatContainer getPlayerStatContainer(UUID uuid) {
        return playerStatMap.get(uuid);
    }

    /**
     * Loads the base stats across all subclass trees into memory.
     *
     * @param uuid         of player to load stats for
     * @param treePosition which subtree are we loading? (1,2,3)
     */
    private void grabBaseStatsFromTree(UUID uuid, int slot, SkillTreePosition treePosition, Jedis jedis) {
        if (RunicCoreAPI.getSkillTree(uuid, slot, treePosition, jedis) == null) return;
        for (Perk perk : RunicCoreAPI.getSkillTree(uuid, slot, treePosition, jedis).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkBaseStat)) continue;
            PerkBaseStat perkBaseStat = (PerkBaseStat) perk;
            Stat stat = perkBaseStat.getStat();
            int amount = perkBaseStat.getBonusAmount() * perkBaseStat.getCurrentlyAllocatedPoints();
            playerStatMap.get(uuid).increaseStat(stat, amount);
        }
    }

    // Fire as HIGHEST so that runic items loads cached stats first for base stat tree grab, and SkillTreeManager loads skill trees
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(CharacterSelectEvent event) {
        StatContainer statContainer = new StatContainer(event.getPlayer());
        UUID uuid = event.getPlayer().getUniqueId();
        playerStatMap.put(event.getPlayer().getUniqueId(), statContainer);
        int slot = event.getCharacterData().getBaseCharacterInfo().getSlot();
        // run event sync (might be able to change this event to async)
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            grabBaseStatsFromTree(uuid, slot, SkillTreePosition.FIRST, event.getJedis());
            grabBaseStatsFromTree(uuid, slot, SkillTreePosition.SECOND, event.getJedis());
            grabBaseStatsFromTree(uuid, slot, SkillTreePosition.THIRD, event.getJedis());
        });
    }

    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        playerStatMap.remove(e.getPlayer().getUniqueId());
    }
}
