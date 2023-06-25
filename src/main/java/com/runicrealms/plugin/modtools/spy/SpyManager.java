package com.runicrealms.plugin.modtools.spy;

import com.runicrealms.RunicChat;
import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.channels.StaffChannel;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SpyAPI;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A class that manages all mods in spy mode
 *
 * @author BoBoBalloon
 * @since 6/24/23
 */
public final class SpyManager implements SpyAPI, Listener {
    private final Map<UUID, SpyInfo> spies;

    public SpyManager() {
        this.spies = new HashMap<>();
    }

    /**
     * A method that returns all the info needed by the plugin for the spy
     *
     * @param spy the moderator spying on another user
     * @return the necessary info or null if the player is not in spy mode
     */
    @Nullable
    public SpyInfo getInfo(@NotNull Player spy) {
        return this.spies.get(spy.getUniqueId());
    }

    /**
     * A method used to set a player into spy mode
     *
     * @param spy    the player in spy mode
     * @param target the player being spied on
     */
    public void setSpy(@NotNull Player spy, @NotNull Player target) {
        if (this.spies.containsKey(spy.getUniqueId())) {
            this.removeSpy(spy);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            SpyInfo info = this.getInfo(spy);

            if (info == null) {
                throw new IllegalStateException("This cannot be run until the spy is registered!");
            }

            if (info.getCenter().distance(spy.getLocation()) < 200) {
                return;
            }

            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                info.setCenter(target.getLocation());
                spy.teleport(info.getCenter(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            });
        }, 20, 100);

        if (!RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().hidePlayer(spy);
        }

        RunicChat.getRunicChatAPI().setWhisperSpy(spy, target, true);

        spy.setGameMode(GameMode.SPECTATOR);
        spy.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        this.spies.put(spy.getUniqueId(), new SpyInfo(target.getUniqueId(), spy.getLocation(), task, target.getLocation()));

        Optional<ChatChannel> optional = RunicChat.getRunicChatAPI().getChatChannels().stream()
                .filter(channel -> channel instanceof StaffChannel)
                .findAny();

        if (optional.isEmpty()) {
            return;
        }

        ChatChannel staffChannel = optional.get();

        for (Player player : staffChannel.getRecipients(spy)) {
            ChatUtils.sendCenteredMessage(player, "&r&9&l" + spy.getName() + " is spying on " + target.getName());
        }
    }

    /**
     * A method that removes the player from the spy list
     *
     * @param spy the moderator spying on another player
     */
    public void removeSpy(@NotNull Player spy) {
        SpyInfo info = this.spies.remove(spy.getUniqueId());

        if (info == null) {
            return;
        }

        info.getTask().cancel();

        if (RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().showPlayer(spy);
        }

        spy.setGameMode(GameMode.ADVENTURE);
        spy.teleport(info.getOrigin(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        Player target = Bukkit.getPlayer(info.getTarget());

        if (target == null) {
            throw new IllegalStateException("Target cannot be null");
        }

        RunicChat.getRunicChatAPI().setWhisperSpy(spy, target, false);
    }

    @EventHandler
    private void onCharacterLeave(@NotNull CharacterQuitEvent event) {
        this.removeSpy(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChatChannelMessage(@NotNull ChatChannelMessageEvent event) {
        for (UUID uuid : this.spies.keySet()) {
            SpyInfo info = this.spies.get(uuid);

            List<UUID> recipients = event.getRecipients().stream().map(Player::getUniqueId).toList();

            if (recipients.contains(info.getTarget()) &&
                    !recipients.contains(uuid) &&
                    event.getSpies().stream().map(Player::getUniqueId).noneMatch(id -> id.equals(uuid))) {
                event.getSpies().add(Bukkit.getPlayer(uuid));
            }
        }
    }
}
