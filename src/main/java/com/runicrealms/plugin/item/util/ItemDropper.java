package com.runicrealms.plugin.item.util;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.listeners.BossTagger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemDropper {

    // todo: create API method, integrate w/ RunicItems 'ri drop'
    // todo: boss logic should just be a damn chest. for mobs, need to fix runic items
    public static void dropLoot(Player pl, String x, String y, String z, String uuid, ItemStack itemStack) {
        Location loc = new Location(pl.getWorld(), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
        UUID mobID = UUID.fromString(uuid);
        if (RunicCore.getMobTagger().isTagged(mobID)) { // if the mob is tagged, drop a prio item
            RunicCore.getMobTagger().dropTaggedLoot(RunicCore.getMobTagger().getTagger(mobID), loc, itemStack);
        } else if (BossTagger.isBoss(mobID)) {
            RunicCore.getBossTagger().dropTaggedBossLoot(mobID, loc, itemStack); // boss loot
        } else {
            pl.getWorld().dropItem(loc, itemStack); // regular drop
        }
    }
}
