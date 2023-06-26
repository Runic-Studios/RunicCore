package com.runicrealms.plugin.loot;

import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class TimedLootChest extends LootChest {

    private final int duration; // in seconds
    private final Consumer<Hologram> hologramEditor;
    private final Player player;

    public TimedLootChest(
            LootChestLocation location,
            LootChestTemplate lootChestTemplate,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            String inventoryTitle,
            Player player,
            int duration,
            Consumer<Hologram> hologramEditor) {
        super(location, lootChestTemplate, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle);
        this.player = player;
        this.duration = duration;
        this.hologramEditor = hologramEditor;
    }

    @Override
    public void openInventory(Player player) {

    }

}
