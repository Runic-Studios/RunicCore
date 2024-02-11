package com.runicrealms.plugin.party;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.party.event.PartyJoinEvent;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.inventivetalent.glow.GlowAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PartyGlowManager implements Listener {

    private static final String SETTINGS_KEY = "settings.party-glow";

    private final Set<UUID> playersWhoSeeGlow = new HashSet<>(); // Excludes players that have manually disabled party glowing

    public PartyGlowManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RunicCommon.getLuckPermsAPI().retrieveData(event.getPlayer().getUniqueId()).thenAccept(data -> {
            if (!event.getPlayer().isOnline()) return;
            if (!data.containsKey(SETTINGS_KEY) || data.getString(SETTINGS_KEY).equals("1")) {
                playersWhoSeeGlow.add(event.getPlayer().getUniqueId());
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersWhoSeeGlow.remove(event.getPlayer().getUniqueId());
    }


    @EventHandler
    public void onPartyJoin(PartyJoinEvent event) { // Called before member is added
        Set<Player> members = event.getParty().getMembersWithLeader();
        setGlowing(event.getJoining(), GlowAPI.Color.DARK_GREEN, members); // Make joiner glow for existing players
        for (Player member : members) {
            setGlowing(member, GlowAPI.Color.DARK_GREEN, member); // Make existing players glow for joiner
        }
    }

    @EventHandler
    public void onPartyLeave(PartyLeaveEvent event) { // Called before the member is removed
        Set<Player> members = event.getParty().getMembersWithLeader(); // Is a copy set
        members.remove(event.getLeaver());
        setGlowing(event.getLeaver(), null, members); // Make leaver no longer glow for party members
        for (Player member : members) {
            setGlowing(member, null, event.getLeaver()); // Make party no longer glow for leaver
        }
    }

    public void toggleCanSeeGlow(Player viewer) {
        if (playersWhoSeeGlow.contains(viewer.getUniqueId())) {
            RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(viewer.getUniqueId(), data -> data.set(SETTINGS_KEY, "1")));
            playersWhoSeeGlow.remove(viewer.getUniqueId());
        } else {
            RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(viewer.getUniqueId(), data -> data.set(SETTINGS_KEY, "0")));
            playersWhoSeeGlow.add(viewer.getUniqueId());
        }
        @Nullable Party party = RunicCore.getPartyAPI().getParty(viewer.getUniqueId());
        if (party != null) {
            for (Player player : party.getMembersWithLeader()) {
                if (player.getUniqueId().equals(viewer.getUniqueId())) continue;
                if (playersWhoSeeGlow.contains(viewer.getUniqueId())) {
                    setGlowing(player, GlowAPI.Color.DARK_GREEN, viewer);
                } else {
                    setGlowing(player, null, viewer);
                }
            }
        }
    }

    public boolean canSeeGlow(Player player) {
        return playersWhoSeeGlow.contains(player.getUniqueId());
    }

    private void setGlowing(Player glower, @Nullable GlowAPI.Color color, Collection<Player> viewers) {
        for (Player player : viewers) {
            setGlowing(glower, color, player);
        }
    }

    private void setGlowing(Player glower, @Nullable GlowAPI.Color color, Player viewer) {
        if (color == null) {
            GlowAPI.setGlowing(glower, false, viewer);
        } else {
            if (!playersWhoSeeGlow.contains(viewer.getUniqueId())) return;
            GlowAPI.setGlowing(glower, color, viewer);
        }
    }

}
