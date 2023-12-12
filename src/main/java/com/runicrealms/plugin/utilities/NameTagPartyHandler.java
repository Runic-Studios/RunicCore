package com.runicrealms.plugin.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.party.event.PartyEvent;
import com.runicrealms.plugin.party.event.PartyJoinEvent;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class NameTagPartyHandler extends PacketAdapter implements Listener {

    private static final ChatColor PARTY_COLOR = ChatColor.DARK_GREEN;

    private static Field paramsField;
    private static Field colorField;
    private static Field membersField;
    private static Object enumChatFormat;

    static {
        try {
            // change per https://github.com/sgtcaze/NametagEdit/blob/master/src/main/java/com/nametagedit/plugin/packets/PacketData.java
            // Decompile paper-1.19.4 source to figure these out
            // Yes i know this should probably use protocol lib, but trust me, I spent a few trying to do that and couldn't figure it out
            // you are very welcome to try
            Class<?> paramsClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$b");
            colorField = paramsClass.getDeclaredField("f");
            colorField.setAccessible(true);
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
            paramsField = packetClass.getDeclaredField("k");
            paramsField.setAccessible(true);
            membersField = packetClass.getDeclaredField("j");
            membersField.setAccessible(true);
            Class<?> enumChatFormatClass = Class.forName("net.minecraft.EnumChatFormat");
            enumChatFormat = Arrays.stream(enumChatFormatClass.getEnumConstants()).filter(constant -> constant.toString().equals(PARTY_COLOR.toString())).findFirst().get();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public NameTagPartyHandler() {
        super(RunicCore.getInstance(), ListenerPriority.HIGHEST, PacketType.Play.Server.SCOREBOARD_TEAM);
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * Handles turning party member's names green client side
     */
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SCOREBOARD_TEAM) return;
        if (colorField == null) return;

        Party party = RunicCore.getPartyAPI().getParty(event.getPlayer().getUniqueId());
        if (party == null) return;

        try {
            Object handle = event.getPacket().getHandle();
            Optional<?> paramsOpt = (Optional<?>) paramsField.get(handle);
            if (paramsOpt.isEmpty()) return;
            Object params = paramsOpt.get();
            Collection<?> members = (Collection<?>) membersField.get(handle);

            boolean similarMember = false;
            for (Object member : members) {
                if (event.getPlayer().getName().equals(member)) continue;
                if (party.getLeader().getName().equals(member)) {
                    similarMember = true;
                    break;
                }
                for (Player partyMember : party.getMembers()) {
                    if (partyMember.getName().equals(member)) {
                        similarMember = true;
                        break;
                    }
                }
            }

            if (similarMember) {
                colorField.set(params, enumChatFormat);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onPartyEvent(PartyEvent event) {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            for (Player player : event.getParty().getMembers()) {
                if (player.isOnline()) refreshNametag(player);
            }
            if (event.getParty().getLeader().isOnline())
                refreshNametag(event.getParty().getLeader());
            if (event instanceof PartyJoinEvent joinEvent) {
                if (joinEvent.getJoining().isOnline()) refreshNametag(joinEvent.getJoining());
            }
            if (event instanceof PartyLeaveEvent leaveEvent) {
                if (leaveEvent.getLeaver().isOnline()) refreshNametag(leaveEvent.getLeaver());

            }
        }, 1L);
    }

    private void refreshNametag(Player player) {
        String currentSuffix = NametagEdit.getApi().getNametag(player).getSuffix();
        NametagEdit.getApi().setSuffix(player, currentSuffix + " ");
        NametagEdit.getApi().setSuffix(player, currentSuffix);
    }

}
