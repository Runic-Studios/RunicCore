package us.fortherealm.plugin.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;

import java.util.ArrayList;
import java.util.UUID;

public class Party {
   private ArrayList<UUID> members = new ArrayList<>();
   private UUID leader;

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

    public UUID getLeader() {
        return this.leader;
    }

    public int getMemberCount() {
       return this.members.size();
    }

    public void sendMessage(String message) {
        for(UUID member : members) {
            Bukkit.getPlayer(member).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void sendMemberMessage(String message) {
        for(UUID member : members) {
            if(!member.equals(leader))
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

   void update() {
       for(UUID member : members) {
           Player player = Bukkit.getPlayer(member);
           if(!player.isOnline()) {
                   this.removeMember(player);
                   this.sendMessage("&3&lParty &7&l> &6" + player.getName() + " &chas been removed from the party! &7Reason: Disconnect");

                   if (this.getLeader() == member) {
                       this.sendMemberMessage("&3&lParty &7&l> &cYour party has been disbanded. &7Reason: Not Enough Members");
                       Main.getPartyManager().disbandParty(this);
                   }
           }
       }
   }
}
