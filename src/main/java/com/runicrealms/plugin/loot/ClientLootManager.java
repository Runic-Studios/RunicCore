package com.runicrealms.plugin.loot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.grid.GridBounds;
import com.runicrealms.plugin.common.util.grid.MultiWorldGrid;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientLootManager {

    private static final MultiWorldGrid<LootChest> chestGrid = new MultiWorldGrid<>(new GridBounds(-4096, -4096, 4096, 4096), (short) 32);
    private static final Map<Player, Map<LootChest, Boolean>> loadedChests = new HashMap<>();

    public ClientLootManager(Collection<? extends LootChest> lootChests) {
        for (LootChest chest : lootChests) {
            chestGrid.insertElement(chest.getLocation().getBukkitLocation(), chest);
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicCore.getInstance(), PacketType.Play.Client.BLOCK_PLACE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.BLOCK_PLACE) {
                    BlockPosition blockPosition = event.getPacket().getBlockPositionModifier().read(0);
                    System.out.println("Player " + event.getPlayer().getName() + " right-clicked block at " + blockPosition);
                    // TODO
//                    BlockPosition position = event.getPacket().getBlockPositionModifier().read(0);
//                    position.
                }
            }
        });
    }

    public void updateChestsForPlayer(Player player) {
        Map<LootChest, Boolean> chests = loadedChests.computeIfAbsent(player, k -> {
            Map<LootChest, Boolean> emptyChests = new HashMap<>();
            for (LootChest chest : RunicCore.getLootAPI().getRegenerativeLootChests()) {
                emptyChests.put(chest, false);
            }
            return emptyChests;
        });
        Set<LootChest> surrounding = chestGrid.getSurroundingElements(player.getLocation(), (short) 2);
        for (Map.Entry<LootChest, Boolean> entry : chests.entrySet()) {
            if (entry.getValue()) {
                if (!surrounding.contains(entry.getKey())) {
                    // DESPAWN CHEST
                    chests.put(entry.getKey(), false);
                }
            } else {
                if (surrounding.contains(entry.getKey())) {
                    // SPAWN CHEST
                    chests.put(entry.getKey(), true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        loadedChests.remove(event.getPlayer());
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        updateChestsForPlayer(event.getPlayer());
    }

}
