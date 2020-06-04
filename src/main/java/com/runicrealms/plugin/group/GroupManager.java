package com.runicrealms.plugin.group;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupManager implements Listener {

    private Set<Group> groups;
    private Map<Player, Group> playerGroups;

    public GroupManager() {
        this.groups = new HashSet<Group>();
        this.playerGroups = new HashMap<Player, Group>();
    }

    public Set<Group> getGroups() {
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // TODO stub
    }

}
