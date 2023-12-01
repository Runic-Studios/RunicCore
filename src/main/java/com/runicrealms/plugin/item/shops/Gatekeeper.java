package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gatekeepers are used in dungeons by players to access next room
 */
public class Gatekeeper extends RunicShopGeneric {

    private final Integer checkpoint;
    private final DungeonLocation dungeonLocation;

    /**
     * @param runicNpcId      id of the runic npc
     * @param requiredItems   a map of templateIDs and their amounts required to access next room
     * @param dungeonLocation an enum value of the dungeon
     * @param checkpoint      a number representing which room checkpoint
     */
    public Gatekeeper(Integer runicNpcId, List<Pair<String, Integer>> requiredItems,
                      DungeonLocation dungeonLocation, Integer checkpoint) {
        super(9, ChatColor.YELLOW + "Gatekeeper", Collections.singletonList(runicNpcId));
        this.dungeonLocation = dungeonLocation;
        this.checkpoint = checkpoint;

        List<RunicShopItem> itemsForSale = new ArrayList<>();
        String key = requiredItems.get(0).first;
        RunicItem runicItem = RunicItemsAPI.generateItemFromTemplate(key);
        itemsForSale.add(new RunicShopItem
                (
                        requiredItems,
                        runicItem.generateItem(),
                        runGatekeeperBuy()
                ));
        setItemsForSale(itemsForSale);
    }

    private RunicItemRunnable runGatekeeperBuy() {
        return player -> {
            Location location = dungeonLocation.getCheckpoints().get(checkpoint);
            player.teleport(location);
            RunicItemShopHelper.clearDungeonKeys(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

            Party party = RunicCore.getPartyAPI().getParty(player.getUniqueId());

            if (party == null) {
                return;
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "party teleport " + player.getName());
            for (Player member : party.getMembersWithLeader()) {
                if (member.getUniqueId().equals(player.getUniqueId())) {
                    continue;
                }

                member.getWorld().playSound(member.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
                member.getWorld().playSound(member.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                RunicItemShopHelper.clearDungeonKeys(member);
            }
        };
    }
}
