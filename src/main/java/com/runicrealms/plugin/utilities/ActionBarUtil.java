package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


/* Copyright (c) dumptruckman 2016
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ActionBarUtil {

    private static final Map<Player, BukkitTask> PENDING_MESSAGES = new HashMap<>();

    /**
     * Sends a message to the player's action bar.
     * <p/>
     * The message will appear above the player's hot bar for 2 seconds and then fade away over 1 second.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param message      the message to send.
     */
    private static void sendActionBarMessage(@NotNull Player bukkitPlayer, @NotNull String message) {
        sendRawActionBarMessage(bukkitPlayer, "{\"text\": \"" + message + "\"}");
    }

    /**
     * Sends a raw message (JSON format) to the player's action bar. Note: while the action bar accepts raw messages
     * it is currently only capable of displaying text.
     * <p/>
     * The message will appear above the player's hot bar for 2 seconds and then fade away over 1 second.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param rawMessage   the json format message to send.
     */
    private static void sendRawActionBarMessage(@NotNull Player bukkitPlayer, @NotNull String rawMessage) {
        CraftPlayer player = (CraftPlayer) bukkitPlayer;
        IChatBaseComponent chatBaseComponent = ChatSerializer.a(rawMessage);
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(chatBaseComponent, ChatMessageType.GAME_INFO, player.getUniqueId());
        player.getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }

    /**
     * Sends a message to the player's action bar that lasts for an extended duration.
     * <p/>
     * The message will appear above the player's hot bar for the specified duration and fade away during the last
     * second of the duration.
     * <p/>
     * Only one long duration message can be sent at a time per player. If a new message is sent via this message
     * any previous messages still being displayed will be replaced.
     *
     * @param bukkitPlayer the player to send the message to.
     * @param message      the message to send.
     * @param duration     the duration the message should be visible for in seconds.
     */
    public static void sendTimedMessage(@NotNull final Player bukkitPlayer, @NotNull final String message,
                                            final int duration) {
        cancelPendingMessages(bukkitPlayer);
        final BukkitTask messageTask = new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count >= (duration - 3)) {
                    this.cancel();
                }
                sendActionBarMessage(bukkitPlayer, ColorUtil.format(message));
                count++;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
        PENDING_MESSAGES.put(bukkitPlayer, messageTask);
    }

    private static void cancelPendingMessages(@NotNull Player bukkitPlayer) {
        if (PENDING_MESSAGES.containsKey(bukkitPlayer)) {
            PENDING_MESSAGES.get(bukkitPlayer).cancel();
        }
    }

//import com.runicrealms.plugin.RunicCore;
//import net.md_5.bungee.api.ChatMessageType;
//import net.md_5.bungee.api.chat.TextComponent;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class ActionBarUtil {
//
//    private static void sendActionBar(Player player, String message) {
//
//        /*private static*/ String nmsver = Bukkit.getServer().getClass().getPackage().getName();
//        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
//
//        if (!player.isOnline()) {
//            return; // Player may have logged out
//        }
//
//        // Call the event, if cancelled don't send Action Bar
//
//        try {
//            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
//            Object craftPlayer = craftPlayerClass.cast(player);
//            Object packet;
//            Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
//            Class<?> packetClass = Class.forName("net.minecraft.server." + nmsver + ".Packet");
//
//                Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
//                Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
//                try {
//                    Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
//                    Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
//                    Object chatMessageType = null;
//                    for (Object obj : chatMessageTypes) {
//                        if (obj.toString().equals("GAME_INFO")) {
//                            chatMessageType = obj;
//                        }
//                    }
//                    Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
//                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatCompontentText, chatMessageType);
//                } catch (ClassNotFoundException cnfe) {
//                    Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
//                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatCompontentText, (byte) 2);
//                }
//
//            Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
//            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
//            Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
//            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
//            Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
//            sendPacketMethod.invoke(playerConnection, packet);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Send the player an action bar message with a duration.
//     * @param duration in SECONDS (method converts to TICKS)
//     */
//    public static void sendTimedMessage(final Player player, final String message, int duration) {
//        long startTime = System.currentTimeMillis();
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (System.currentTimeMillis() - startTime > (duration * 1000))
//                    this.cancel();
//                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.format(message)));
//            }
//        }.runTaskTimer(RunicCore.getInstance(), 0, 10);//Asynchronously
////        duration *= 20; // convert to ticks
////
////        sendActionBar(player, ColorUtil.format(message));
////
////        if (duration >= 0) {
////            // Sends empty message at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
////            new BukkitRunnable() {
////                @Override
////                public void run() {
////                    sendActionBar(player, "");
////                }
////            }.runTaskLater(RunicCore.getInstance(), duration + 1);
////        }
////
////        // Re-sends the messages every 3 seconds so it doesn't go away from the player's screen.
////        while (duration > 40) {
////            duration -= 40;
////            new BukkitRunnable() {
////                @Override
////                public void run() {
////                    sendActionBar(player, ColorUtil.format(message));
////                }
////            }.runTaskLater(RunicCore.getInstance(), duration);
////        }
////    }
//    }
}
