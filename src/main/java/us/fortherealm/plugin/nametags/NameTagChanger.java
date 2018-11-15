package us.fortherealm.plugin.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_13_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.parties.Party;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.UUID;

public class NameTagChanger {

    private Plugin plugin = Main.getInstance();

    // TODO: update the scoreboard on nametag change
    // TODO: are all these arrayLists gonna cause memory leaks/performance issues, or are we okay?

    public void changeNameParty(Party party, Player p, String newName){

        // create an array of players to be used later
        ArrayList<Player> partyMembers = new ArrayList<>();

        // builds an ArrayList of Players from our array of UUIDS
        for (UUID memberID: party.getMembers()) {

            // grabs member from the UUID
            Player member = Bukkit.getPlayer(UUID.fromString(memberID.toString()));

            // adds new (Player) member to ArrayList
            partyMembers.add(member);
        }

        // for each player in the party (converted from UUIDs to players)
        for (Player pl : partyMembers) {

            // -------------------------------------
            // ignore the sender - DON'T CHANGE THIS
            if (pl == p) continue;
            // -------------------------------------

            // removes the player
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer)p).getHandle()));

            // gets the player's game profile
            GameProfile gp = ((CraftPlayer)p).getProfile();

            // changes the player's game profile
            try {
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);

                nameField.set(gp, newName);

            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }

            // adds the player back with the new signature
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer)p).getHandle()));
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(p.getEntityId()));
            ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer)p).getHandle()));
        }
    }

    public void showPartyNames(Party party, Player p) {

        // create an array of players to be used later
        ArrayList<Player> partyMembers = new ArrayList<>();

        // builds an ArrayList of Players from our array of UUIDS
        for (UUID memberID : party.getMembers()) {

            // grabs member from the UUID
            Player member = Bukkit.getPlayer(UUID.fromString(memberID.toString()));

            // adds new (Player) member to ArrayList
            partyMembers.add(member);
        }

        // for each player in the party (converted from UUIDs to players)
        for (Player partyMember : partyMembers) {

            // -------------------------------------
            // ignore the sender - DON'T CHANGE THIS
            if (partyMember == p) continue;
            // -------------------------------------

            // removes the player
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) partyMember).getHandle()));

            // get the party member's game profile
            GameProfile gp = ((CraftPlayer) partyMember).getProfile();

            // change their game profile
            try {
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

//                // set their name in config to original nameField
//                if (!plugin.getConfig().isSet(p.getUniqueId() + ".info.nameField")){
//                    plugin.getConfig().set(p.getUniqueId() + ".info.nameField", nameField);
//                    plugin.saveConfig();
//                    plugin.reloadConfig();
//                }

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);




                // sets the player's name field to the new value
                nameField.set(gp, ChatColor.GREEN + partyMember.getName());


            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }

            // adds the party member back with new signature for our sender
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) partyMember).getHandle()));
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(partyMember.getEntityId()));
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) partyMember).getHandle()));
        }
    }

    public void changeNameGlobal(Player p, String newName) {

        // for every online player
        for (Player online : Bukkit.getOnlinePlayers()) {

            // -------------------------------------
            // ignore the sender - DON'T CHANGE THIS
            if (online == p) continue;
            // -------------------------------------

            // removes the player
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) p).getHandle()));

            // gets the player's game profile
            GameProfile gp = ((CraftPlayer) p).getProfile();

            // changes the player's game profile
            try {
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);

                nameField.set(gp, newName);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }

            // adds the player back with the new signature
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) p).getHandle()));
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(p.getEntityId()));
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) p).getHandle()));
        }
    }
}
