package com.runicrealms.plugin.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;

import java.util.ArrayList;
import java.util.UUID;

public class Party {

    // instanced array, variable
   private ArrayList<UUID> members = new ArrayList<>();
   private UUID leader;
   private Plugin plugin = RunicCore.getInstance();

   // constructor
   public Party(UUID leader) {
       this.members.add(leader);
       this.leader = leader;
   }

   public void addMember(Player player) {
       this.members.add(player.getUniqueId());
   }

   public void addMember(UUID uuid) {
       this.members.add(uuid);
   }

   public void removeMember(Player player) {
       this.members.remove(player.getUniqueId());
   }

   public void removeMember(UUID uuid) {
       this.members.remove(uuid);
   }

   public void removeAllMembers() {
       this.members.clear();
   }

   public void setLeader(Player player) {
       this.leader = player.getUniqueId();
   }

   public void setLeader(UUID uuid) {
       this.leader = uuid;
   }

   // todo: public void getAvgRating

    public UUID getLeader() {
        return this.leader;
    }

    public int getLeaderIndex() { return members.indexOf(this.leader); }

    public int getMemberIndex(UUID memberID) { return members.indexOf(memberID); }

    public UUID getMemberUUID(int index) { return members.get(index ); }

    public int getPartySize() {
       return this.members.size();
    }

    public void sendMessage(String message) {
        for(UUID member : members) {
            Bukkit.getPlayer(member).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void sendMembersMessage(String message) {
        for(UUID member : members) {
            if(!member.equals(leader))
                Bukkit.getPlayer(member).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void sendOtherMembersMessage(String message, UUID senderID) {
        for(UUID member : members) {
            if(!member.equals(senderID))
                Bukkit.getPlayer(member).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

   public boolean hasMember(Player player) {
       for(UUID member : members)
       {
           if(player.getUniqueId().equals(member))
           {
               return true;
           }
       }
       return false;
   }

   public boolean hasMember(UUID uuid) {
       for(UUID member : members)
       {
           if(uuid.equals(member))
               return true;
       }
       return false;
   }

   public ArrayList<UUID> getMembers()
   {
       return this.members;
   }

   public ArrayList<Player> getPlayerMembers() {

       ArrayList<Player> partyPlayers = new ArrayList<>();

       for (UUID uuid : this.getMembers()) {
           Player member = Bukkit.getPlayer(uuid);
           partyPlayers.add(member);
       }
       return partyPlayers;
   }

   public ArrayList<String> getPartyNames() {

       ArrayList<String> partyNames = new ArrayList<>();

       for  (Player member : this.getPlayerMembers()) {
           partyNames.add(member.getName());
       }
       return partyNames;
   }

   void update() {
       for(UUID member : members) {
           Player player = Bukkit.getPlayer(member);
           if(!player.isOnline()) {
                   this.removeMember(player);
                   this.sendMessage("&3&lParty &7&l> &6" + player.getName() + " &chas been removed from the party! &7Reason: Disconnect");

                   if (this.getLeader() == member) {
                       this.sendMembersMessage("&3&lParty &7&l> &cYour party has been disbanded. &7Reason: Not Enough Members");
                       RunicCore.getPartyManager().disbandParty(this);
                   }
           }
       }
   }
}
