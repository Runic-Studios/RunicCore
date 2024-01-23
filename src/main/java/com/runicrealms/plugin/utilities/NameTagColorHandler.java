package com.runicrealms.plugin.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.party.event.PartyEvent;
import com.runicrealms.plugin.party.event.PartyJoinEvent;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NameTagColorHandler extends PacketAdapter implements Listener {

    private static final Map<String, NameColor> globalColor = new HashMap<>();

    private static final Field paramsField;
    private static final Field colorField;
    private static final Field membersField;

    static {
        try {
            // change per https://github.com/sgtcaze/NametagEdit/blob/master/src/main/java/com/nametagedit/plugin/packets/PacketData.java
            // Decompile paper-1.19.4 source to figure these out
            // This will be changed to use ProtocolLib in the future, part of RR587
            Class<?> paramsClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$b");
            colorField = paramsClass.getDeclaredField("f");
            colorField.setAccessible(true);
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
            paramsField = packetClass.getDeclaredField("k");
            paramsField.setAccessible(true);
            membersField = packetClass.getDeclaredField("j");
            membersField.setAccessible(true);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load NMS for nametags", exception);
        }
    }

    public NameTagColorHandler() {
        super(RunicCore.getInstance(), ListenerPriority.HIGHEST, PacketType.Play.Server.SCOREBOARD_TEAM);
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(this).start();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    private static void refreshNametag(Player player) {
        String currentSuffix = NametagEdit.getApi().getNametag(player).getSuffix();
        NametagEdit.getApi().setSuffix(player, currentSuffix + " ");
        NametagEdit.getApi().setSuffix(player, currentSuffix);
    }

    public static void setGlobalColor(Player player, NameColor nameColor) {
        globalColor.put(player.getName(), nameColor);
        refreshNametag(player);
    }

    /**
     * Handles turning party member's names green client side
     */
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SCOREBOARD_TEAM) return;
        if (colorField == null) return;

        Optional<?> opt = event.getPacket().getMeta("customhandled");
        if (opt.isPresent() && (boolean) opt.get()) return;

        try {
            Object handle = event.getPacket().getHandle();
            Collection<?> members = (Collection<?>) membersField.get(handle);
            if (members.size() == 0) return;

            NameColor defaultGlobal = null;
            for (Object member : members) {
                NameColor color = globalColor.get((String) member);
                if (color != null) {
                    defaultGlobal = color;
                    break;
                }
            }

            boolean similarMemberParty = false;
            Party party = RunicCore.getPartyAPI().getParty(event.getPlayer().getUniqueId());
            if (party != null) {
                outerFor:
                for (Object member : members) {
                    if (event.getPlayer().getName().equals(member)) continue;
                    if (party.getLeader().getName().equals(member)) {
                        similarMemberParty = true;
                        break;
                    }
                    for (Player partyMember : party.getMembers()) {
                        if (partyMember.getName().equals(member)) {
                            similarMemberParty = true;
                            break outerFor;
                        }
                    }
                }
            }

            if (similarMemberParty || defaultGlobal != null) {
                // Prevent the default behavior which is to reuse packets and send to other people.
                // Instead we forcefully clone the packet, send it, cancel this one, and then prevent the new one from
                // being processed with a meta tag.
                PacketContainer clone = event.getPacket().deepClone();
                Object cloneHandle = clone.getHandle();
                Optional<?> cloneParamsOpt = (Optional<?>) paramsField.get(cloneHandle);
                if (cloneParamsOpt.isEmpty()) return;
//                    throw new IllegalStateException("Scoreboard Team packet cloned params are empty but we deep cloned???");
                Object cloneParams = cloneParamsOpt.get();

                if (similarMemberParty) {
                    colorField.set(cloneParams, NameColor.PARTY.sourceColor);
                } else {
                    colorField.set(cloneParams, defaultGlobal.sourceColor);
                }
                PacketContainer container = PacketContainer.fromPacket(cloneHandle);
                container.setMeta("customhandled", true);
                ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), container);
                event.setCancelled(true);
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
        }, 1L); // run after we have removed the player/added the player to the party
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        globalColor.remove(event.getPlayer().getName());
    }

    // Run after onCharacterLoaded in DatabaseManager, needs to be character loaded
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        setGlobalColor(event.getPlayer(), RunicCommon.getPvPAPI().isOutlaw(event.getPlayer()) ? NameColor.OUTlAW : NameColor.DEFAULT);
    }

    public static class NameColor {

        public static final NameColor OUTlAW = new NameColor(ChatColor.DARK_RED);
        public static final NameColor DEFAULT = new NameColor(ChatColor.WHITE);
        private static final NameColor PARTY = new NameColor(ChatColor.DARK_GREEN);

        private final Object sourceColor;

        private NameColor(ChatColor chatColor) {
            try {
                Class<?> enumChatFormatClass = Class.forName("net.minecraft.EnumChatFormat");
                this.sourceColor = Arrays.stream(enumChatFormatClass.getEnumConstants()).filter(constant -> constant.toString().equals(chatColor.toString())).findFirst().get();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to retrieve nametag color NMS for " + chatColor.name(), exception);
            }
        }

    }

}
