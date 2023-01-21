package com.runicrealms.plugin.player.stat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.StatAPI;
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

    /**
     * Loads the base stats across all subclass trees into memory.
     *
     * @param uuid         of player to load stats for
     * @param treePosition which subtree are we loading? (1,2,3)
     */
    private void grabBaseStatsFromTree(UUID uuid, int slot, SkillTreePosition treePosition, Jedis jedis) {
        if (RunicCore.getSkillTreeAPI().getSkillTree(uuid, slot, treePosition) == null) return;
        for (Perk perk : RunicCore.getSkillTreeAPI().getSkillTree(uuid, slot, treePosition, jedis).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkBaseStat)) continue;
            PerkBaseStat perkBaseStat = (PerkBaseStat) perk;
            Stat stat = perkBaseStat.getStat();
            int amount = perkBaseStat.getBonusAmount() * perkBaseStat.getCurrentlyAllocatedPoints();
            playerStatMap.get(uuid).increaseStat(stat, amount);
        }
    }

    /**
     * Fire as HIGHEST so that runic items loads cached stats
     * first for base stat tree grab, and SkillTreeManager loads skill trees
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(CharacterSelectEvent event) {
        StatContainer statContainer = new StatContainer(event.getPlayer());
        UUID uuid = event.getPlayer().getUniqueId();
        playerStatMap.put(event.getPlayer().getUniqueId(), statContainer);
        int slot = event.getCharacterData().getBaseCharacterInfo().getSlot();
        // run event sync (might be able to change this event to async)
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
                grabBaseStatsFromTree(uuid, slot, SkillTreePosition.FIRST, jedis);
                grabBaseStatsFromTree(uuid, slot, SkillTreePosition.SECOND, jedis);
                grabBaseStatsFromTree(uuid, slot, SkillTreePosition.THIRD, jedis);
            }
        });
    }

    @EventHandler
    public void onQuit(CharacterQuitEvent event) {
        playerStatMap.remove(event.getPlayer().getUniqueId());
    }
}
