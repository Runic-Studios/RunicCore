package com.runicrealms.plugin.parties;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Group {

    private Set<Player> members;
    private Player leader;
    private GroupPurpose purpose;

    public Group(GroupPurpose purpose) {
        this.purpose = purpose;
        this.members = new HashSet<Player>();
    }

    public Player getLeader() {
        return this.leader;
    }

    public void addMember(Player player) {
        this.members.add(player);
    }

    public void removeMember(Player player) {
        this.members.remove(player);
    }

    public void disband() {
        this.members.clear();
    }

    public void setLeader(Player player) {
        this.leader = player;
    }

    public boolean hasMember(Player player) {
        return this.members.contains(player) || this.leader == player;
    }

    public Set<Player> getMembersWithLeader() {
        Set<Player> membersWithLeader = new HashSet<Player>(this.members);
        membersWithLeader.add(this.leader);
        return membersWithLeader;
    }

}
