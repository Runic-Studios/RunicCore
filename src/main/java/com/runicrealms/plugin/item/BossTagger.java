package com.runicrealms.plugin.item;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BossTagger {

    private static final int DAMAGE_PERCENT = 10;

    private HashMap<UUID, List<UUID>> taggedBosses;

    public BossTagger() {
        taggedBosses = new HashMap<>();
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent e) {

        if (!e.getMob().hasFaction()) return;
        if (!e.getMobType().getFaction().equalsIgnoreCase("boss")) return;

        // delayed task by 2s to clear the hashmap w/ the boss
    }

    // when combat engaged w/ mob that has "boss", keep track of its max hp and add to each player who deals at least X% (10%?) damage.
    // create static method that takes an entity UUID that gives a list of players who should receive loot
    // when the boss dies, run a command with the UUID of the boss to retrieve its list of players, then give the corresponding loot table.
}
