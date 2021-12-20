package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Collections;
import java.util.LinkedHashSet;

public class Gatekeeper extends RunicShopGeneric {

    private final Integer checkpoint;
    private final DungeonLocation dungeonLocation;

    public Gatekeeper(Integer runicNpcId, String currencyTemplateId, Integer price, DungeonLocation dungeonLocation, Integer checkpoint) {
        super(9, ChatColor.YELLOW + "Gatekeeper", Collections.singletonList(runicNpcId));
        this.dungeonLocation = dungeonLocation;
        this.checkpoint = checkpoint;
        RunicItem runicItem = RunicItemsAPI.generateItemFromTemplate(currencyTemplateId);
        setItemsForSale(new LinkedHashSet<>(Collections.singleton(new RunicShopItem
                (
                        price,
                        currencyTemplateId,
                        runicItem.generateItem(),
                        runicItem.getDisplayableItem().getDisplayName(),
                        runGatekeeperBuy()
                ))));
    }

    private RunicItemRunnable runGatekeeperBuy() {
        return player -> {
            // todo: particles, sounds
            Location location = dungeonLocation.getCheckpoints().get(checkpoint);
            player.teleport(location);
            Bukkit.dispatchCommand(player, "party teleport " + player.getName());
        };
    }


}
