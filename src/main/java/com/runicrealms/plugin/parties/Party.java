package com.runicrealms.plugin.parties;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
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

    public Invite addInvite(Player player) {
        Invite invite = new Invite(player, this);
        this.invites.add(invite);
        return invite;
    }

    public boolean acceptMemberInvite(Player player) {
        this.members.add(player);
        Invite playerInvite = null;
        for (Invite invite : this.invites) {
            if (invite.getPlayer() == player) {
                playerInvite = invite;
                break;
            }
        }
        if (playerInvite != null) {
            playerInvite.inviteAccepted();
            this.invites.remove(playerInvite);
            return true;
        }
        return false;
    }

    public void kickMember(Player player) {
        this.members.remove(player);
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

    public Invite getInvite(Player player) {
        for (Invite invite : this.invites) {
            if (invite.getPlayer() == player) {
                return invite;
            }
        }
        return null;
    }

    public void sendMessageInChannel(String message) {
        RunicCore.getPartyChatChannel().getRecipients(this.leader).forEach(player -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', RunicCore.getPartyChatChannel().getConsolePrefix() + message));
        });
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
                if (!task.isCancelled()) {
                    this.task.cancel();
                }
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
