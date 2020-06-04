package com.runicrealms.plugin.group;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Group {

    private Set<Player> members;
    private GroupPurpose purpose;

    public Group(GroupPurpose purpose) {
        this.purpose = purpose;
        this.members = new HashSet<Player>();
    }

    public GroupPurpose getPurpose() {
        return this.purpose;
    }

    public void addMember(Player player) {
        this.members.add(player);
    }

    public void removeMember(Player player) {
        this.members.remove(player);
    }

    public boolean hasMember(Player player) {
        return this.members.contains(player);
    }

}
