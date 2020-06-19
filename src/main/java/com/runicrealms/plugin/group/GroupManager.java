package com.runicrealms.plugin.group;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.gui.GroupCreateChoosePurposeGui;
import com.runicrealms.plugin.group.gui.GroupCreateChooseTypeGui;
import com.runicrealms.plugin.group.gui.GroupInfoGui;
import com.runicrealms.plugin.group.gui.GroupJoinGui;
import com.runicrealms.plugin.group.gui.GroupMainGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class GroupManager implements Listener {

    private LinkedHashMap<GroupPurpose, Group> groups;
    private Map<Player, Group> playerGroups;

    public void registerGuiEvents() {
        Bukkit.getPluginManager().registerEvents(new GroupMainGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupInfoGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupJoinGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupCreateChooseTypeGui(), RunicCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new GroupCreateChoosePurposeGui(), RunicCore.getInstance());
        GroupMainGui.initInventory();
        GroupCreateChooseTypeGui.initInventory();
    }

    public GroupManager() {
        this.groups = new LinkedHashMap<GroupPurpose, Group>(); // Important that it is linked!
        this.playerGroups = new HashMap<Player, Group>();
    }

    public LinkedHashMap<GroupPurpose, Group> getGroups() {
        return this.groups;
    }

    public void createGroup(Player player, GroupPurpose purpose) {
        Group group = new Group(purpose);
        group.addMember(player);
        this.groups.put(purpose, group);
        this.updatePlayerGroup(player, group);
    }

    public void addToGroup(Player player, Group group) {
        group.addMember(player);
        this.updatePlayerGroup(player, group);
    }

    public void removeFromGroup(Player player, Group group) {
        group.removeMember(player);
        if (group.getMembers().size() == 0) {
            this.groups.remove(group);
        }
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
