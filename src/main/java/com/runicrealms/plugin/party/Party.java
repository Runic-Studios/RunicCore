package com.runicrealms.plugin.party;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.event.LeaveReason;
import com.runicrealms.plugin.party.event.PartyEvent;
import com.runicrealms.plugin.party.event.PartyJoinEvent;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class Party {

    private final Set<Player> members;
    private final Set<Invite> invites;
    private Player leader;

    /**
     * Create a player party! Also calls the custom party event.
     * @param leader player who created the party
     */
    public Party(Player leader) {
        this.members = new HashSet<>();
        this.invites = new HashSet<>();
        this.leader = leader;
        PartyEvent partyEvent = new PartyEvent(this);
        Bukkit.getPluginManager().callEvent(partyEvent);
    }

    public Player getLeader() {
        return this.leader;
    }

    public Invite addInvite(Player player) {
        Invite invite = new Invite(player, this);
        this.invites.add(invite);
        return invite;
    }

    /**
     * Method called when a player accepts an invite to a party.
     * @param player who accepted invite
     * @return whether player joined party
     */
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
            PartyJoinEvent partyJoinEvent = new PartyJoinEvent(this, player);
            Bukkit.getPluginManager().callEvent(partyJoinEvent);
            return true;
        }
        return false;
    }

    /**
     * Called when a player is kicked from the party
     * @param player to be kicked
     * @param leaveReason reason the player left
     */
    public void kickMember(Player player, LeaveReason leaveReason) {
        this.members.remove(player);
        PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(this, player, leaveReason);
        Bukkit.getPluginManager().callEvent(partyLeaveEvent);
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

    public void removeInvite(Player player) {
        Invite remove = null;
        for (Invite invite : this.invites) {
            if (invite.getPlayer() == player) {
                remove = invite;
                break;
            }
        }
        if (remove != null) {
            this.invites.remove(remove);
        }
    }

    public void sendMessageInChannel(String message) {
        RunicCore.getPartyChatChannel().getRecipients(this.leader).forEach(player ->
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', RunicCore.getPartyChatChannel().getConsolePrefix() + message)));
    }

    public static class Invite {

        private final Player player;
        private final Party party;
        private final BukkitTask task;

        public Invite(Player player, Party party) {
            this.player = player;
            this.party = party;
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    Invite.this.party.removeInvite(Invite.this.player);
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
