package com.runicrealms.plugin.item.util;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemDropper {

    public static void dropLoot(Player pl, String x, String y, String z, String uuid, ItemStack itemStack) {
        Location loc = new Location(pl.getWorld(), Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
        UUID mobID = UUID.fromString(uuid);
        if (RunicCore.getMobTagger().getIsTagged(mobID)) { // if the mob is tagged, drop a prio item
            RunicCore.getMobTagger().dropTaggedLoot(RunicCore.getMobTagger().getTagger(mobID), loc, itemStack);
        } else if (RunicCore.getBossTagger().isBoss(mobID)) {
            RunicCore.getBossTagger().dropTaggedBossLoot(mobID, loc, itemStack); // boss loot
        } else {
            pl.getWorld().dropItem(loc, itemStack); // regular drop
        }
    }
}
