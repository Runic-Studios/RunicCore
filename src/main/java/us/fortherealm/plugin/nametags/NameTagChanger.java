package us.fortherealm.plugin.nametags;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_13_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.UUID;

public class NameTagChanger {

    private Plugin plugin = Main.getInstance();
    private ScoreboardHandler sbh = new ScoreboardHandler();

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

        // for each player in the party (converted from UUIDs to Player objects)
        for (Player member : partyMembers) {

            // grab the player's stored name
            Object storedNameP = plugin.getConfig().get(p.getUniqueId() + ".info.name");
            String pNameToString = storedNameP.toString();

            // ignore the sender - change only their tag name
            if (member == p) {
                //p.setPlayerListName(ChatColor.GREEN + pNameToString);
            } else {

                // change the target Player 'p's name for all members!
                changeName(member, p, newName);
            }

            // update health displays
            updateHealth(member);
        }

        // null check
        if (p.getScoreboard() == null) { return; }

        // update player health display
        updateHealth(p);
    }

    public void showPartyNames(Party party, Player joiner) {

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

            // update health displays
            updateHealth(partyMember);

            // ignore the current player in the loop if it is the name-changed player, only update their tablist name
            if (partyMember == joiner) {

                continue;
            }

            // removes the player
            ((CraftPlayer) joiner).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) partyMember).getHandle()));

            // get the party member's game profile
            GameProfile gp = ((CraftPlayer) partyMember).getProfile();

            // change their game profile
            try {
                Field nameField = GameProfile.class.getDeclaredField("name");
                nameField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);


                // sets the player's name field to the new value
                String storedName = plugin.getConfig().get(partyMember.getUniqueId() + ".info.name").toString();
                nameField.set(gp, ChatColor.GREEN + storedName);

            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new IllegalStateException(ex);
            }

            // adds the party member back with new signature for our sender
            ((CraftPlayer) joiner).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) partyMember).getHandle()));
            ((CraftPlayer) joiner).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(partyMember.getEntityId()));
            ((CraftPlayer) joiner).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) partyMember).getHandle()));

            // null check
            if (partyMember.getScoreboard() == null) { return; }

            // update player health display
            updateHealth(partyMember);
        }
    }

    public void changeNameGlobal(Player p, String newName) {

        // for every online player
        for (Player online : Bukkit.getOnlinePlayers()) {

            // ignore the current player in the loop is the name-changed player, only update their tablist name
            if (online == p) { continue; }


                // update health displays for all OTHER players
                updateHealth(online);

                changeName(online, p, newName);

        }

        // null check
        if (p.getScoreboard() == null) { return; }

        // update player health display
        updateHealth(p);
    }

    private static void changeName(Player observer, Player changed, String newName) {

        // removes the player
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) changed).getHandle()));

        // gets the player's game profile
        GameProfile gp = ((CraftPlayer) changed).getProfile();

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
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) changed).getHandle()));
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(changed.getEntityId()));
        ((CraftPlayer) observer).getHandle().playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(((CraftPlayer) changed).getHandle()));
    }

    private void updateHealth(Player pl) {

        // update health bar and scoreboard
        new BukkitRunnable() {
            @Override
            public void run() {
                sbh.updateSideInfo(pl);
            }
        }.runTaskLater(plugin, 1);
    }
}
