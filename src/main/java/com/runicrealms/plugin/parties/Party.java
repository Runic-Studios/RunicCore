package com.runicrealms.plugin.parties;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class Party {

    private Set<Player> members;
    private Set<Invite> invites;
    private Player leader;

    public Party(Player leader) {
        this.members = new HashSet<Player>();
        this.invites = new HashSet<Invite>();
        this.leader = leader;
    }

    public Player getLeader() {
        return this.leader;
    }

    public void inviteMember(Player player) {
        this.invites.add(new Invite(player, this));
    }

    public void acceptMemberInvite(Player player) {

    }

    public void kickMember(Player player) {
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

    public Set<Player> getMembers() {
        return this.members;
    }

    public Set<Player> getMembersWithLeader() {
        Set<Player> membersWithLeader = new HashSet<Player>(this.members);
        membersWithLeader.add(this.leader);
        return membersWithLeader;
    }

    public Integer getSize() {
        return this.members.size() + 1;
    }

    public Set<Invite> getInvites() {
        return this.invites;
    }

    public static class Invite {

        private Player player;
        private Party party;
        private BukkitTask task;

        public Invite(Player player, Party party) {
            this.player = player;
            this.party = party;
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!Invite.this.party.getInvites().contains(Invite.this)) {
                        Invite.this.party.getInvites().remove(Invite.this);
                    }
                }
            }.runTaskLater(RunicCore.getInstance(), 60 * 20);
        }

        public void inviteAccepted() {
            if (this.task != null) {
                this.task.cancel();
            }
        }

        public Player getPlayer() {
            return this.player;
        }

        public Party getParty() {
            return this.party;
        }

    }

}
