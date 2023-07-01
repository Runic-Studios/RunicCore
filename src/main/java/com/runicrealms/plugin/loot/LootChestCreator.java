package com.runicrealms.plugin.loot;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@CommandAlias("lootchest")
@CommandPermission("runic.op")
public class LootChestCreator extends BaseCommand implements Listener {

    private final Map<UUID, LootChestInfo> creatingChests = new ConcurrentHashMap<>();
    private final Set<UUID> deletingChests = new HashSet<>();

    public LootChestCreator() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("chest-templates", context ->
                RunicCore.getLootAPI().getChestTemplates().stream().map(LootChestTemplate::getIdentifier).collect(Collectors.toList()));
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicCore.getInstance(), ListenerPriority.LOW, PacketType.Play.Client.USE_ITEM) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
                    onUsePacketEvent(event);
                }
            }
        }).start();
    }

    private static boolean isInt(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static String joinArgs(String[] array, int startPos) {
        if (array == null || startPos < 0 || startPos >= array.length) throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder();
        for (int i = startPos; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static BlockFace getDirectionFacingPlayer(Location playerLocation) {
        float yaw = playerLocation.getYaw();
        if (yaw < 0) yaw += 360;
        BlockFace direction;
        if (yaw >= 315 || yaw < 45) {
            direction = BlockFace.NORTH;
        } else if (yaw < 135) {
            direction = BlockFace.EAST;
        } else if (yaw < 225) {
            direction = BlockFace.SOUTH;
        } else {
            direction = BlockFace.WEST;
        }
        return direction;
    }

    @Subcommand("create")
    @CommandCompletion("@chest-templates")
    public void onCommandCreate(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <min-level> <item-min-level> <item-max-level> [regeneration-time] [title]");
            return;
        }
        String template = args[0];
        if (!RunicCore.getLootAPI().isLootChestTemplate(template)) {
            player.sendMessage(ChatColor.RED + "That is not a valid chest template identifier!");
            return;
        }
        LootChestTemplate chestTemplate = RunicCore.getLootAPI().getLootChestTemplate(args[0]);
        if (!isInt(args[1]) || !isInt(args[2]) || !isInt(args[3])) {
            player.sendMessage(ChatColor.RED + "min-level, item-min-level, and item-max-level must be integers!");
            return;
        }
        int minLevel = Integer.parseInt(args[1]);
        int itemMinLevel = Integer.parseInt(args[2]);
        int itemMaxLevel = Integer.parseInt(args[3]);

        Integer regenerationTime = null;
        String title = null;

        if (args.length >= 5) {
            if (!isInt(args[4])) {
                player.sendMessage(ChatColor.RED + "Regeneration time must be an integer!");
                return;
            }
            regenerationTime = Integer.parseInt(args[4]);
        }
        if (args.length >= 6) title = ColorUtil.format(joinArgs(args, 5));

        if (regenerationTime == null) {
            if (template.equalsIgnoreCase("common-chest")) {
                regenerationTime = 600;
            } else if (template.equalsIgnoreCase("uncommon-chest")) {
                regenerationTime = 900;
            } else if (template.equalsIgnoreCase("rare-chest")) {
                regenerationTime = 1200;
            } else if (template.equalsIgnoreCase("epic-chest")) {
                regenerationTime = 2700;
            } else {
                player.sendMessage(ChatColor.RED + "Since you are creating a loot chest with a custom template, please specify the [regeneration-time].");
                player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
                return;
            }
        }
        if (title == null) {
            if (template.equalsIgnoreCase("common-chest")) {
                title = "Common Loot Chest";
            } else if (template.equalsIgnoreCase("uncommon-chest")) {
                title = "Uncommon Loot Chest";
            } else if (template.equalsIgnoreCase("rare-chest")) {
                title = "Rare Loot Chest";
            } else if (template.equalsIgnoreCase("epic-chest")) {
                title = "Epic Loot Chest";
            } else {
                player.sendMessage(ChatColor.RED + "Since you are creating a loot chest with a custom template, please specify the [title].");
                player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
                return;
            }
        }
        deletingChests.remove(player.getUniqueId());

        creatingChests.put(player.getUniqueId(), new LootChestInfo(chestTemplate, minLevel, itemMinLevel, itemMaxLevel, regenerationTime, title));
        player.sendMessage(ChatColor.GREEN + "Right click a chest to turn it into a loot chest, or type /lootchest cancel.");
    }

    @Subcommand("delete")
    public void onCommandDelete(Player player) {
        deletingChests.add(player.getUniqueId());
        creatingChests.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Right click a loot chest to revert it back to a normal block, or type /lootchest cancel.");
    }

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /lootchest create <chest-template> <item-min-level> <item-max-level> [regeneration-time] [title]");
        player.sendMessage(ChatColor.RED + "Or: /lootchest delete");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        creatingChests.remove(event.getPlayer().getUniqueId());
    }

    @Subcommand("cancel")
    public void onCommandCancel(Player player) {
        if (creatingChests.containsKey(player.getUniqueId())) {
            creatingChests.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Canceled creating loot chest!");
        } else {
            if (deletingChests.contains(player.getUniqueId())) {
                deletingChests.remove(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Canceled deleting loot chest!");
            } else {
                player.sendMessage(ChatColor.RED + "You are not currently creating or deleting a loot chest, nothing to cancel");
            }
        }
    }

    private void onUsePacketEvent(PacketEvent event) {
        if (deletingChests.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            BlockPosition position = event.getPacket().getBlockPositionModifier().readSafely(0);
            if (position == null) return;
            Location location = position.toLocation(event.getPlayer().getWorld());
            RegenerativeLootChest lootChest = RunicCore.getLootAPI().getRegenerativeLootChest(location);
            if (lootChest != null) {
                deletingChests.remove(event.getPlayer().getUniqueId());
                RunicCore.getLootAPI().deleteRegenerativeLootChest(lootChest);
                event.getPlayer().sendMessage(ChatColor.GREEN + "Removed loot chest!");
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "That is not a loot chest! Type /lootchest cancel to cancel deleting a loot chest.");
            }
        } else if (creatingChests.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            BlockPosition position = event.getPacket().getBlockPositionModifier().readSafely(0);
            if (position == null) return;
            Location location = position.toLocation(event.getPlayer().getWorld()).add(0, 1, 0);
            LootChestInfo chestInfo = creatingChests.get(event.getPlayer().getUniqueId());
            RunicCore.getLootAPI().createRegenerativeLootChest(new RegenerativeLootChest(
                            new LootChestPosition(location, getDirectionFacingPlayer(event.getPlayer().getLocation())),
                            chestInfo.template,
                            new LootChestConditions(),
                            chestInfo.minLevel,
                            chestInfo.itemMinLevel, chestInfo.itemMaxLevel,
                            chestInfo.regenerationTime,
                            chestInfo.title
                    )
            );
            creatingChests.remove(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Added loot chest!");
        }
    }

    private record LootChestInfo(
            LootChestTemplate template,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            int regenerationTime,
            String title) {
    }

}
