package com.runicrealms.plugin.modtools;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VanishManager implements VanishAPI, Listener {

    private final Set<Player> vanishedPlayers = new HashSet<>();

    public VanishManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(RunicCore.getInstance(), ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                ping.setPlayersOnline(Bukkit.getOnlinePlayers().size() - RunicCore.getVanishAPI().getVanishedPlayers().size());
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player vanished : vanishedPlayers) {
            event.getPlayer().hidePlayer(RunicCore.getInstance(), vanished);
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (vanishedPlayers.contains(event.getPlayer())) {
            for (Player online : Bukkit.getOnlinePlayers())
                online.showPlayer(RunicCore.getInstance(), event.getPlayer());
            vanishedPlayers.remove(event.getPlayer());
        }
    }

    @Override
    public Collection<Player> getVanishedPlayers() {
        return vanishedPlayers;
    }

    @Override
    public void hidePlayer(Player player) {
        if (vanishedPlayers.contains(player))
            throw new IllegalArgumentException("Cannot hide player that is already vanished.");
        vanishedPlayers.add(player);
        for (Player online : Bukkit.getOnlinePlayers()) online.hidePlayer(RunicCore.getInstance(), player);
        RunicCore.getTabAPI().refreshAllTabLists();
    }

    @Override
    public void showPlayer(Player player) {
        if (!vanishedPlayers.contains(player))
            throw new IllegalArgumentException("Cannot show player that is not vanished.");
        vanishedPlayers.remove(player);
        for (Player online : Bukkit.getOnlinePlayers()) online.showPlayer(RunicCore.getInstance(), player);
        RunicCore.getTabAPI().refreshAllTabLists();
    }
}
