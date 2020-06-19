package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GroupManager implements Listener {

    private LinkedHashSet<Group> groups;
    private Map<Player, Group> playerGroups;

    public GroupManager() {
        this.groups = new LinkedHashSet<Group>(); // Important that it is linked!
        this.playerGroups = new HashMap<Player, Group>();
    }

    public LinkedHashSet<Group> getGroups() {
        return this.groups;
    }

    public void updatePlayerGroup(Player player, Group group) {
        if (group == null) {
            this.playerGroups.remove(player);
        } else {
            this.playerGroups.put(player, group);
        }
    }

    public Group getPlayerGroup(Player player) {
        if (this.playerGroups.containsKey(player)) {
            return this.playerGroups.get(player);
        }
        return null;
    }

    public boolean canJoinGroup(Player player) {
        return RunicCore.getPartyManager().getPlayerParty(player) == null && this.getPlayerGroup(player) == null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // TODO stub
    }

}
