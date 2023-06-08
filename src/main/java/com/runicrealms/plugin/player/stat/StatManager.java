package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.StatAPI;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.runicitems.Stat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatManager implements Listener, StatAPI {

    private final HashMap<UUID, StatContainer> playerStatMap;

    public StatManager() {
        playerStatMap = new HashMap<>();
        RunicCore.getInstance().getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public int getPlayerDexterity(UUID uuid) {
        if (RunicCore.getStatAPI().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return playerStatMap.get(uuid).getStat(Stat.DEXTERITY);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getPlayerIntelligence(UUID uuid) {
        if (RunicCore.getStatAPI().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return playerStatMap.get(uuid).getStat(Stat.INTELLIGENCE);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public StatContainer getPlayerStatContainer(UUID uuid) {
        return playerStatMap.get(uuid);
    }

    @Override
    public int getPlayerStrength(UUID uuid) {
        if (playerStatMap.get(uuid) == null) return 0;
        try {
            return playerStatMap.get(uuid).getStat(Stat.STRENGTH);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getPlayerVitality(UUID uuid) {
        if (RunicCore.getStatAPI().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return playerStatMap.get(uuid).getStat(Stat.VITALITY);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getPlayerWisdom(UUID uuid) {
        if (RunicCore.getStatAPI().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return playerStatMap.get(uuid).getStat(Stat.WISDOM);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getStat(UUID uuid, String statName) {
        Stat stat = Stat.getFromIdentifier(statName);
        try {
            return playerStatMap.get(uuid).getStat(stat);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Loads the base stats across all subclass trees into memory.
     *
     * @param uuid         of player to load stats for
     * @param treePosition which subtree are we loading? (1,2,3)
     */
    private void grabBaseStatsFromTree(UUID uuid, int slot, SkillTreePosition treePosition) {
        Map<SkillTreePosition, SkillTreeData> skillTreeDataMap = RunicCore.getSkillTreeAPI().getSkillTreeDataMap(uuid, slot);
        if (skillTreeDataMap == null) return;
        SkillTreeData skillTreeData = skillTreeDataMap.get(treePosition);
        for (Perk perk : skillTreeData.getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkBaseStat perkBaseStat)) continue;
            Stat stat = perkBaseStat.getStat();
            int amount = perkBaseStat.getBonusAmount() * perkBaseStat.getCurrentlyAllocatedPoints();
            playerStatMap.get(uuid).increaseStat(stat, amount);
        }
    }

    /**
     * Fires AFTER PlayerManager in RunicItems can set up stats
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onLoad(CharacterLoadedEvent event) {
        StatContainer statContainer = new StatContainer(event.getPlayer());
        UUID uuid = event.getPlayer().getUniqueId();
        playerStatMap.put(event.getPlayer().getUniqueId(), statContainer);
        int slot = event.getCharacterSelectEvent().getSlot();
        grabBaseStatsFromTree(uuid, slot, SkillTreePosition.FIRST);
        grabBaseStatsFromTree(uuid, slot, SkillTreePosition.SECOND);
        grabBaseStatsFromTree(uuid, slot, SkillTreePosition.THIRD);

    }

    @EventHandler
    public void onQuit(CharacterQuitEvent event) {
        playerStatMap.remove(event.getPlayer().getUniqueId());
    }
}
