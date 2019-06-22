package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class GuildListeners implements Listener {

    // ------------------------------------------------------------------
    // GUILDS
    @EventHandler
    public void onGuildCreate(GuildCreateEvent e) {
        Bukkit.broadcastMessage("when does this get called.. on completion, okay. so I need to cancel the event if they don't have the gold");
        new BukkitRunnable() {
            @Override
            public void run() {
                RunicCore.getTabListManager().setupTab(e.getPlayer());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onGuildJoin(GuildJoinEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player guildy : Guilds.getApi().getGuild(e.getPlayer().getUniqueId()).getOnlineAsPlayers()) {
                    RunicCore.getTabListManager().setupTab(guildy);
                }
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onGuildLeave(GuildLeaveEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player guildy : e.getGuild().getOnlineAsPlayers()) {
                    RunicCore.getTabListManager().setupTab(guildy);
                }
                RunicCore.getTabListManager().setupTab(e.getPlayer());
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 1L);
    }
    // ------------------------------------------------------------------
}
