package com.runicrealms.plugin.loot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.grid.GridBounds;
import com.runicrealms.plugin.common.util.grid.MultiWorldGrid;
import com.runicrealms.plugin.loot.chest.LootChest;
import com.runicrealms.plugin.loot.chest.LootChestInventory;
import com.runicrealms.plugin.loot.chest.RegenerativeLootChest;
import com.runicrealms.plugin.loot.chest.TimedLootChest;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles displaying the client sided loot chest to players
 * and opening/closing chest inventories
 */
public class ClientLootManager implements Listener {

    private final MultiWorldGrid<LootChest> chestGrid = new MultiWorldGrid<>(new GridBounds(-4096, -4096, 4096, 4096), (short) 32); // Load chests efficiently
    private final Map<Player, Map<Location, ClientLootChest>> loadedChests = new HashMap<>(); // Chests that each player can see in the world
    private final Map<UUID, ConcurrentHashMap<RegenerativeLootChest, Long>> lastOpened = new HashMap<>(); // For cooldowns

    public ClientLootManager(@NotNull Collection<? extends LootChest> lootChests) {
        for (LootChest chest : lootChests) {
            chestGrid.insertElement(chest.getPosition().getLocation(), chest);
        }
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> updateClientChests(), 0, 20 * 4);

        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicCore.getInstance(), ListenerPriority.HIGH, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && event.getPacket().getHands().read(0) == EnumWrappers.Hand.MAIN_HAND) {
                    onUseItemPacket(event);
                }
            }
        }).start();
    }

    /**
     * A method that hides and reveals nearby chests
     *
     * @param player the player to update chests for
     */
    public void updateClientChests(@NotNull Player player) {
        Map<Location, ClientLootChest> chests = loadedChests.computeIfAbsent(player, key -> {
            Map<Location, ClientLootChest> emptyChests = new HashMap<>();
            for (LootChest chest : RunicCore.getLootAPI().getRegenerativeLootChests()) {
                emptyChests.put(chest.getPosition().getLocation(), new ClientLootChest(chest, false));
            }
            return emptyChests;
        });

        Set<LootChest> surrounding = chestGrid.getSurroundingElements(player.getLocation(), (short) 2);
        for (ClientLootChest clientChest : chests.values()) {
            LootChest chest = clientChest.lootChest;
            boolean displayed = clientChest.displayed;

            if (!chest.shouldUpdateDisplay()) {
                continue;
            }

            if (displayed && !surrounding.contains(chest)) {
                chest.hideFromPlayer(player);
                clientChest.displayed = false;
                continue;
            }

            if (displayed || !surrounding.contains(chest) || (chest instanceof RegenerativeLootChest regenChest && isOnCooldown(player, regenChest))) {
                continue;
            }

            chest.showToPlayer(player);
            clientChest.displayed = true;
        }
    }

    public boolean isOnCooldown(@NotNull Player player, @NotNull RegenerativeLootChest lootChest) {
        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(player.getUniqueId());
        if (playerOpened == null) return false;
        long lastOpenedTime = playerOpened.get(lootChest);
        int timeLeft = (int) ((lastOpenedTime + lootChest.getRegenerationTime() * 1000L - System.currentTimeMillis()) / 1000);
        return timeLeft > 0;
    }

    public void updateClientChests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateClientChests(player);
        }
    }

    public void displayTimedLootChest(@NotNull Player player, @NotNull TimedLootChest chest) {
        Location identifier = chest.getPosition().getLocation();
        loadedChests.get(player).put(identifier, new ClientLootChest(chest, true));
        chest.beginDisplay(player, () -> {
            Map<Location, ClientLootChest> loaded = loadedChests.get(player);
            if (loaded != null) {
                loaded.remove(identifier);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        loadedChests.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            updateClientChests(event.getPlayer());
            ConcurrentHashMap<RegenerativeLootChest, Long> playerOpened = new ConcurrentHashMap<>();

            for (RegenerativeLootChest lootChest : RunicCore.getLootAPI().getRegenerativeLootChests()) {
                playerOpened.put(lootChest, 0L);
            }
            lastOpened.put(event.getPlayer().getUniqueId(), playerOpened);
        }, 10); //add 10 tick delay so the chests will actually be displayed to the player
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (loadedChests.containsKey(event.getPlayer())) {
            updateClientChests(event.getPlayer());
        }
    }

    /* //not sure what this listener is even used for, we handle it with protocollib -BoBo
    @EventHandler(priority = EventPriority.NORMAL) // first
    public void onChestInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (!event.hasBlock()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        event.setCancelled(true);

        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(event.getPlayer().getUniqueId());
        if (playerOpened == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        RegenerativeLootChest lootChest = RunicCore.getLootAPI().getRegenerativeLootChest(location);
        if (lootChest == null) return;

        if (player.getLevel() < lootChest.getMinLevel()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must be at least level " + lootChest.getMinLevel() + " to open this.");
            return;
        }

        long lastOpenedTime = playerOpened.get(lootChest);
        int timeLeft = (int) ((lastOpenedTime + lootChest.getRegenerationTime() * 1000L - System.currentTimeMillis()) / 1000);
        if (timeLeft > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " seconds to loot this chest again!");
            return;
        }

        playerOpened.put(lootChest, System.currentTimeMillis());
        player.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        lootChest.openInventory(player);
    }
     */

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        this.lastOpened.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLootChestInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof LootChestInventory)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        if (RunicItemsAPI.containsBlockedTag(runicItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLootChestClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player
                && event.getView().getTopInventory().getHolder() instanceof LootChestInventory holder) {
            holder.close(player);
            player.playSound(holder.getLootChest().getPosition().getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            holder.getLootChest().hideFromPlayer(player);
        }
    }

    private void onUseItemPacket(@NotNull PacketEvent event) {
        MovingObjectPositionBlock position = event.getPacket().getMovingBlockPositions().readSafely(0);
        if (position == null) {
            return;
        }

        Location location = position.getBlockPosition().toLocation(event.getPlayer().getWorld());

        Map<Location, ClientLootChest> loaded = loadedChests.get(event.getPlayer());
        if (loaded == null) {
            return;
        }

        ClientLootChest chest = loaded.get(location);
        if (chest == null) {
            return;
        }

        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(event.getPlayer().getUniqueId());
        if (playerOpened == null) {
            RunicCore.getInstance().getLogger().severe("loot chest last opened data is not in memory for " + event.getPlayer().getName());
            event.getPlayer().sendMessage(ColorUtil.format("&cThere was an error getting your loot chest data from memory. Please report this to an admin!"));
            return;
        }

        if (!(chest.lootChest instanceof RegenerativeLootChest regenerativeLootChest)) {
            return;
        }

        if (this.isOnCooldown(event.getPlayer(), regenerativeLootChest)) {
            regenerativeLootChest.hideFromPlayer(event.getPlayer());
            return;
        }

        playerOpened.put(regenerativeLootChest, System.currentTimeMillis());
        event.setCancelled(true);
        RunicCore.getTaskChainFactory().newChain()
                .sync(regenerativeLootChest::playOpenAnimation)
                .delay(20)
                .sync(() -> {
                    Map<Location, ClientLootChest> chests = loadedChests.get(event.getPlayer());
                    ClientLootChest client = chests.get(regenerativeLootChest.getPosition().getLocation());

                    if (client == null) {
                        return;
                    }

                    regenerativeLootChest.openInventory(event.getPlayer());
                    client.displayed = false;
                    regenerativeLootChest.hideFromPlayer(event.getPlayer());
                })
                .execute();
    }

    private static class ClientLootChest {
        private final LootChest lootChest;
        private boolean displayed;

        private ClientLootChest(@NotNull LootChest lootChest, boolean displayed) {
            this.lootChest = lootChest;
            this.displayed = displayed;
        }
    }
}